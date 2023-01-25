package com.lay.ipc

import com.google.gson.Gson
import com.lay.ipc.client.Channel
import com.lay.ipc.model.Parameters
import com.lay.ipc.model.REQUEST_TYPE
import com.lay.ipc.server.Registry
import com.lay.ipc.service.IPCService
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * author: qinlei
 * create by: 2023/1/24 22:07
 * description:
 */
class IPCInvocationHandler(
    val service: Class<out IPCService>,
    val serviceId: String?
) : InvocationHandler {

    private val gson = Gson()

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {

        //执行客户端发送方法请求
        val response = Channel.getDefault()
            .send(
                REQUEST_TYPE.INVOKE_METHOD.ordinal,
                service,
                serviceId,
                method?.name ?: "",
                args
            )
        //拿到服务端返回的结果
        if (response != null && response.result ==200) {
            //反序列化得到结果
            return gson.fromJson(response.value, method?.returnType)
        }


        return null
    }

}