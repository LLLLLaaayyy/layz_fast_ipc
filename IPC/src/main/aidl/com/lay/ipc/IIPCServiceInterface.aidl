// IIPCServiceInterface.aidl
package com.lay.ipc;
// Declare any non-default types here with import statements
import com.lay.ipc.model.Response;
import com.lay.ipc.model.Request;

interface IIPCServiceInterface {
    Response send(in Request request);
}