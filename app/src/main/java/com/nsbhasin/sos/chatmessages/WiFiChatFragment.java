
package com.nsbhasin.sos.chatmessages;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.nsbhasin.sos.R;
import com.nsbhasin.sos.DestinationDeviceTabList;
import com.nsbhasin.sos.socketmanagers.ChatManager;
import com.nsbhasin.sos.services.ServiceList;
import com.nsbhasin.sos.chatmessages.waitingtosend.WaitingToSendQueue;
import com.nsbhasin.sos.services.WiFiP2pService;

import lombok.Getter;
import lombok.Setter;

public class WiFiChatFragment extends Fragment {

    private static final String TAG = "WiFiChatFragment";

    @Getter
    @Setter
    private Integer tabNumber;
    @Getter
    @Setter
    private static boolean firstStartSendAddress;
    @Getter
    @Setter
    private boolean grayScale = true;
    @Getter
    private final List<String> items = new ArrayList<>();

    private TextView chatLine;

    @Getter
    @Setter
    private ChatManager chatManager;
    private WiFiChatMessageListAdapter adapter = null;

    public interface AutomaticReconnectionListener {
        public void reconnectToService(WiFiP2pService wifiP2pService);
    }

    /**
     * Method to obtain a new Fragment's instance.
     *
     * @return This Fragment instance.
     */
    public static WiFiChatFragment newInstance() {
        return new WiFiChatFragment();
    }

    /**
     * Default Fragment constructor.
     */
    public WiFiChatFragment() {
    }

    public void sendForcedWaitingToSendQueue() {

        Log.d(TAG, "sendForcedWaitingToSendQueue() called");

        String combineMessages = "";
        List<String> listCopy = WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber);
        for (String message : listCopy) {
            if (!message.equals("") && !message.equals("\n")) {
                combineMessages = combineMessages + "\n" + message;
            }
        }
        combineMessages = combineMessages + "\n";

        Log.d(TAG, "Queued message to send: " + combineMessages);

        if (chatManager != null) {
            if (!chatManager.isDisable()) {
                chatManager.write((combineMessages).getBytes());
                WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).clear();
            } else {
                Log.d(TAG, "Chatmanager disabled, impossible to send the queued combined message");
            }

        }
    }

    public void pushMessage(String readMessage) {
        items.add(readMessage);
        adapter.notifyDataSetChanged();
    }

    public void updateChatMessageListAdapter() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Method that add the text in the chatLine EditText to the WaitingToSendQueue and try to reconnect
     * to the service associated to the device of this tab, with index tabNumber.
     */
    private void addToWaitingToSendQueueAndTryReconnect() {
        //add message to the waiting to send queue
        WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).add(chatLine.getText().toString());

        //try to reconnect
        WifiP2pDevice device = DestinationDeviceTabList.getInstance().getDevice(tabNumber - 1);
        if (device != null) {
            WiFiP2pService service = ServiceList.getInstance().getServiceByDevice(device);
            Log.d(TAG, "device address: " + device.deviceAddress + ", service: " + service);

            //call reconnectToService in MainActivity
            ((AutomaticReconnectionListener) getActivity()).reconnectToService(service);

        } else {
            Log.d(TAG, "addToWaitingToSendQueueAndTryReconnect device == null, i can't do anything");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatmessage_list, container, false);

        chatLine = (TextView) view.findViewById(R.id.txtChatLine);
        ListView listView = (ListView) view.findViewById(R.id.list);

        adapter = new WiFiChatMessageListAdapter(getActivity(), R.id.txtChatLine, this);
        listView.setAdapter(adapter);

        view.findViewById(R.id.sendMessage).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (chatManager != null) {
                            if (!chatManager.isDisable()) {
                                Log.d(TAG, "chatmanager state: enable");

                                //send message to the ChatManager's outputStream.
                                chatManager.write(chatLine.getText().toString().getBytes());
                            } else {
                                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);

                                addToWaitingToSendQueueAndTryReconnect();
                            }

                            pushMessage("Me: " + chatLine.getText().toString());
                            chatLine.setText("");
                        } else {
                            Log.d(TAG, "chatmanager is null");
                        }
                    }
                });

        return view;
    }


}
