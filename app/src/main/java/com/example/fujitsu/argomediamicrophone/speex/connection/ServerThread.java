package com.example.fujitsu.argomediamicrophone.speex.connection;

import android.content.Context;
import android.view.View;
import com.sapphire.microphone.C;
import com.sapphire.microphone.L;
import com.sapphire.microphone.R;
import com.sapphire.microphone.Util;
import com.sapphire.microphone.dialogs.AcceptConnectionDialog;
import com.sapphire.microphone.exceptions.CancelException;
import com.sapphire.microphone.exceptions.SameRoleException;
import com.sapphire.microphone.interfaces.SocketActionListener;
import com.sapphire.microphone.model.RoleRequest;
import com.sapphire.microphone.util.PrefUtil;
import com.sapphire.microphone.util.SocketWrapper;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread implements View.OnClickListener {
    private Socket socket = null;
    private ObjectInputStream is = null;
    private ObjectOutputStream os = null;
    private final SocketActionListener listener;
    private static final int CONNECTION_WAIT_TIME = 6000;
    private final Context context;

    public ServerThread(final SocketActionListener listener, final Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void run() {
        final boolean isMic = PrefUtil.isMic();
        try {
            waitForConnection();
            if (isMic) {
                showAcceptDialog();
            } else {
                dialogDismissed(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onConnectionError(false);
        }
    }

    private void showAcceptDialog() {
        if (PrefUtil.isMic()) {
            Util.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AcceptConnectionDialog acceptConnectionDialog = new AcceptConnectionDialog(context);
                    acceptConnectionDialog.setOnClickListener(ServerThread.this);
                    acceptConnectionDialog.setCancelable(false);
                    acceptConnectionDialog.show();
                }
            });
        }
    }

    private void waitForConnection() throws Exception {
        ServerSocket serverSocket = new ServerSocket();
        for (int port : C.CONNECTION_PORTS) {
            try {
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(port));
                L.e("listening port: " + port);
                break;
            } catch (Exception e) {
                L.e("port fail: " + port);
                e.printStackTrace();
            }
        }
        if (!serverSocket.isBound() && !serverSocket.isClosed()) {
            serverSocket.close();
            throw new Exception("socket is not bound");
        }
        serverSocket.setSoTimeout(CONNECTION_WAIT_TIME);
        try {socket = serverSocket.accept();} catch (Exception ignored) {}
        try {serverSocket.close();} catch (Exception ignored) {}
        if (socket != null) {
            socket.setTcpNoDelay(true);
            return;
        }
        throw new Exception("nobody has connected");
    }

    private void init(final boolean canConnect) throws Exception {
        os = new ObjectOutputStream(socket.getOutputStream());
        is = new ObjectInputStream(socket.getInputStream());
        final String message = is.readUTF();
        if (message.equals(C.INIT_CONNECTION)) {
            if (canConnect)
                os.writeUTF(C.RESPONSE_OK);
            else
                os.writeUTF(C.RESPONSE_CANCEL);
            os.flush();
            if (!canConnect)
                throw new ConnectException();
            else return;
        } else if (message.equals(C.CONNECTION_CANCELED)) {
            os.writeUTF(C.RESPONSE_OK);
            os.flush();
            throw new CancelException();
        }
        throw new Exception("wrong init connection response");
    }

    private void respondRoleRequest() throws Exception {
        final String localRole = PrefUtil.getRole();
        final String remoteRole = is.readUTF();
        os.writeUTF(localRole);
        os.flush();
        final RoleRequest roleRequest = new RoleRequest();
        roleRequest.setLocalRole(localRole);
        roleRequest.setRemoteRole(remoteRole);
        if (!roleRequest.isEqualRoles()) {
            return;
        }
        throw new SameRoleException(roleRequest);
    }

    private void sendPreperaMessage() throws Exception {
        if (PrefUtil.getRole().equals(C.MIC)) {
            os.writeUTF(C.PREPARE_RECEIVING_AUDIO_STREAM);
            os.flush();
            final String response = is.readUTF();
            if (response.equals(C.RESPONSE_OK)) {
                return;
            }
        } else {
            final String message = is.readUTF();
            if (message.equals(C.PREPARE_RECEIVING_AUDIO_STREAM)) {
                os.writeUTF(C.RESPONSE_OK);
                os.flush();
                return;
            }
        }
        throw new Exception("wrong prepare response code");
    }

    @Override
    public void onClick(View v) {
        dialogDismissed(v.getId() == R.id.ok);
    }

    private void dialogDismissed(final boolean canConnect) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    init(canConnect);
                    respondRoleRequest();
                    sendPreperaMessage();
                } catch (SameRoleException e) {
                    e.printStackTrace();
                    listener.onSameRole(e.getRoleRequest());
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onConnectionError(e instanceof CancelException);
                    return;
                }
                listener.onConnectionSuccess(new SocketWrapper(socket));
            }
        }).start();

    }
}
