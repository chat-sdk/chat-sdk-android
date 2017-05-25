package co.chatsdk.dao.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by KyleKrueger on 13.04.2017.
 */

public class ProcessForQueryHandler {
    public static String processForQuery(String query){
        return StringUtils.isBlank(query) ? "" : query.replace(" ", "").toLowerCase();
    }
}
