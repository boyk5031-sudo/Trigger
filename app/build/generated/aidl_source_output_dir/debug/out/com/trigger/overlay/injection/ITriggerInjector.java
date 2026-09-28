/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.trigger.overlay.injection;
// High-speed binder interface that runs in Shizuku shell context (uid 2000)
// All coordinates MUST be physical display pixels (not dp, not view coords)
public interface ITriggerInjector extends android.os.IInterface
{
  /** Default implementation for ITriggerInjector. */
  public static class Default implements com.trigger.overlay.injection.ITriggerInjector
  {
    // Simple tap: DOWN -> UP with minimal latency
    @Override public void injectTap(float x, float y, int displayId) throws android.os.RemoteException
    {
    }
    // Low-level lifecycle for custom gestures / hold / swipe
    @Override public void injectDown(float x, float y, int displayId, long downTime) throws android.os.RemoteException
    {
    }
    @Override public void injectMove(float x, float y, int displayId, long downTime) throws android.os.RemoteException
    {
    }
    @Override public void injectUp(float x, float y, int displayId, long downTime) throws android.os.RemoteException
    {
    }
    // Multi-touch: inject second finger (for games that need 2 pointers)
    @Override public void injectPointerDown(int pointerId, float x, float y, int displayId, long downTime) throws android.os.RemoteException
    {
    }
    @Override public void injectPointerUp(int pointerId, float x, float y, int displayId, long downTime) throws android.os.RemoteException
    {
    }
    // Ping & cleanup
    @Override public int getVersion() throws android.os.RemoteException
    {
      return 0;
    }
    @Override public void destroy() throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.trigger.overlay.injection.ITriggerInjector
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.trigger.overlay.injection.ITriggerInjector interface,
     * generating a proxy if needed.
     */
    public static com.trigger.overlay.injection.ITriggerInjector asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.trigger.overlay.injection.ITriggerInjector))) {
        return ((com.trigger.overlay.injection.ITriggerInjector)iin);
      }
      return new com.trigger.overlay.injection.ITriggerInjector.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_injectTap:
        {
          float _arg0;
          _arg0 = data.readFloat();
          float _arg1;
          _arg1 = data.readFloat();
          int _arg2;
          _arg2 = data.readInt();
          this.injectTap(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_injectDown:
        {
          float _arg0;
          _arg0 = data.readFloat();
          float _arg1;
          _arg1 = data.readFloat();
          int _arg2;
          _arg2 = data.readInt();
          long _arg3;
          _arg3 = data.readLong();
          this.injectDown(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_injectMove:
        {
          float _arg0;
          _arg0 = data.readFloat();
          float _arg1;
          _arg1 = data.readFloat();
          int _arg2;
          _arg2 = data.readInt();
          long _arg3;
          _arg3 = data.readLong();
          this.injectMove(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_injectUp:
        {
          float _arg0;
          _arg0 = data.readFloat();
          float _arg1;
          _arg1 = data.readFloat();
          int _arg2;
          _arg2 = data.readInt();
          long _arg3;
          _arg3 = data.readLong();
          this.injectUp(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_injectPointerDown:
        {
          int _arg0;
          _arg0 = data.readInt();
          float _arg1;
          _arg1 = data.readFloat();
          float _arg2;
          _arg2 = data.readFloat();
          int _arg3;
          _arg3 = data.readInt();
          long _arg4;
          _arg4 = data.readLong();
          this.injectPointerDown(_arg0, _arg1, _arg2, _arg3, _arg4);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_injectPointerUp:
        {
          int _arg0;
          _arg0 = data.readInt();
          float _arg1;
          _arg1 = data.readFloat();
          float _arg2;
          _arg2 = data.readFloat();
          int _arg3;
          _arg3 = data.readInt();
          long _arg4;
          _arg4 = data.readLong();
          this.injectPointerUp(_arg0, _arg1, _arg2, _arg3, _arg4);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getVersion:
        {
          int _result = this.getVersion();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_destroy:
        {
          this.destroy();
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.trigger.overlay.injection.ITriggerInjector
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      // Simple tap: DOWN -> UP with minimal latency
      @Override public void injectTap(float x, float y, int displayId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(x);
          _data.writeFloat(y);
          _data.writeInt(displayId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_injectTap, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      // Low-level lifecycle for custom gestures / hold / swipe
      @Override public void injectDown(float x, float y, int displayId, long downTime) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(x);
          _data.writeFloat(y);
          _data.writeInt(displayId);
          _data.writeLong(downTime);
          boolean _status = mRemote.transact(Stub.TRANSACTION_injectDown, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void injectMove(float x, float y, int displayId, long downTime) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(x);
          _data.writeFloat(y);
          _data.writeInt(displayId);
          _data.writeLong(downTime);
          boolean _status = mRemote.transact(Stub.TRANSACTION_injectMove, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void injectUp(float x, float y, int displayId, long downTime) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(x);
          _data.writeFloat(y);
          _data.writeInt(displayId);
          _data.writeLong(downTime);
          boolean _status = mRemote.transact(Stub.TRANSACTION_injectUp, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      // Multi-touch: inject second finger (for games that need 2 pointers)
      @Override public void injectPointerDown(int pointerId, float x, float y, int displayId, long downTime) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(pointerId);
          _data.writeFloat(x);
          _data.writeFloat(y);
          _data.writeInt(displayId);
          _data.writeLong(downTime);
          boolean _status = mRemote.transact(Stub.TRANSACTION_injectPointerDown, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void injectPointerUp(int pointerId, float x, float y, int displayId, long downTime) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(pointerId);
          _data.writeFloat(x);
          _data.writeFloat(y);
          _data.writeInt(displayId);
          _data.writeLong(downTime);
          boolean _status = mRemote.transact(Stub.TRANSACTION_injectPointerUp, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      // Ping & cleanup
      @Override public int getVersion() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getVersion, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void destroy() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_destroy, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_injectTap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_injectDown = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_injectMove = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_injectUp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_injectPointerDown = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_injectPointerUp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_getVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_destroy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
  }
  public static final java.lang.String DESCRIPTOR = "com.trigger.overlay.injection.ITriggerInjector";
  // Simple tap: DOWN -> UP with minimal latency
  public void injectTap(float x, float y, int displayId) throws android.os.RemoteException;
  // Low-level lifecycle for custom gestures / hold / swipe
  public void injectDown(float x, float y, int displayId, long downTime) throws android.os.RemoteException;
  public void injectMove(float x, float y, int displayId, long downTime) throws android.os.RemoteException;
  public void injectUp(float x, float y, int displayId, long downTime) throws android.os.RemoteException;
  // Multi-touch: inject second finger (for games that need 2 pointers)
  public void injectPointerDown(int pointerId, float x, float y, int displayId, long downTime) throws android.os.RemoteException;
  public void injectPointerUp(int pointerId, float x, float y, int displayId, long downTime) throws android.os.RemoteException;
  // Ping & cleanup
  public int getVersion() throws android.os.RemoteException;
  public void destroy() throws android.os.RemoteException;
}
