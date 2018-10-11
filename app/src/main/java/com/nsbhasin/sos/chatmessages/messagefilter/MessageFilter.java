package com.nsbhasin.sos.chatmessages.messagefilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;

public class MessageFilter {

    //this list contains all the messages in lowercase to filter messages
    @Getter
    private List<String> lowerCaseBlackList;

    private static final MessageFilter instance = new MessageFilter();

    /**
     * Method to get the instance of this class.
     *
     * @return instance of this class.
     */
    public static MessageFilter getInstance() {
        return instance;
    }

    /**
     * Private constructor, because is a singleton class.
     */
    private MessageFilter() {

        lowerCaseBlackList = new ArrayList<>();
    }

    /**
     * Method to know if the message must be filtered.
     *
     * @param message String message to check
     * @return boolean: true if the message is not valid because is filtered, false otherwise.
     * @throws MessageException Exception throwed if the message is null or if it's too short or
     *                          if it's blacklisted.
     */
    public boolean isFiltered(String message) throws MessageException {
        if (message == null) {
            throw new MessageException(MessageException.Reason.NULLMESSAGE);
        }

        if (message.length() <= 1) {
            throw new MessageException(MessageException.Reason.MESSAGETOOSHORT);
        }

        String[] chunckList = message.toLowerCase(Locale.US).split(" ");

        for (int i = 0; i < chunckList.length; i++) {
            if (lowerCaseBlackList.contains(chunckList[i])) {
                throw new MessageException(MessageException.Reason.MESSAGEBLACKLISTED);
            }
        }
        return false;
    }

}
