package com.lay.ipc.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.lay.ipc.IIPCServiceInterface
import com.lay.ipc.model.Parameters
import com.lay.ipc.model.Request
import com.lay.ipc.model.Response
import com.lay.ipc.service.IPCService
import java.util.concurrent.ConcurrentHashMap

/**
 * author: qinlei
 * create by: 2023/1/24 08:33
 * description:
 */
class Channel {

    //====================================
    /**每个服务对应的Binder对象*/
    private val binders: ConcurrentHashMap<Class<out IPCService>, IIPCServiceInterface> by lazy {
        ConcurrentHashMap()
    }

    private val gson = Gson()

    //====================================

    /**
     * 绑定服务
     *
     */
    fun connect(
        context: Context,
        pkgName: String,
        action: String = "",
        service: Class<out IPCService>
    ) {
        val intent = Intent()
        if (pkgName.isEmpty()) {
            intent.setClass(context, service)
        } else {
            intent.setPackage(pkgName)
            intent.setAction(action)
//            intent.setClass(context, service)
        }
        //绑定服务
        val result = context.bindService(intent, IpcServiceConnection(service), Context.BIND_AUTO_CREATE)
        Log.e("TAG","bind result $result")
    }

    inner class IpcServiceConnection(val simpleService: Class<out IPCService>) : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("TAG","onServiceConnected")
            val mService = IIPCServiceInterface.Stub.asInterface(service) as IIPCServiceInterface
            binders[simpleService] = mService
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //断连之后，直接移除即可
            binders.remove(simpleService)
        }
    }


    fun send(
        type: Int,
        service: Class<out IPCService>,
        serviceId: String?,
        methodName: String,
        params: Array<out Any>?
    ): Response? {
        //创建请求
        val request = Request(type, serviceId, methodName, parseArgs(params))
        //发起请求
        return try {
            binders[service]?.send(request)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseArgs(args: Array<out Any>?): Array<Parameters?>? {
        if (args == null || args.isEmpty()) {
            return null
        }
        val paramsArray = arrayOfNulls<Parameters>(args.size)
        args.forEachIndexed { index, any ->
            paramsArray[index] = Parameters(any.javaClass.name, gson.toJson(any))
        }
        return paramsArray
    }


    companion object {
        private val instance by lazy {
            Channel()
        }

        /**
         * 获取单例对象
         */
        fun getDefault(): Channel {
            return instance
        }
    }
}