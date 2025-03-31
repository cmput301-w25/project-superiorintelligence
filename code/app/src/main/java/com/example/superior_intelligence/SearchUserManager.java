package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for user search operations.
 */
public class SearchUserManager {

    /**
     * Searches for users whose usernames start with the given prefix,
     * excluding the current user.
     *
     * @param prefix The search prefix to match against usernames
     * @param allUsers The complete list of users to search through
     * @param currentUsername The username of the current user to exclude from results
     * @return List of matching {@link UserHelper} objects, empty list if no matches found
     */
    public static List<UserHelper> searchPrefix(String prefix, List<UserHelper> allUsers, String currentUsername) {
        List<UserHelper> result = new ArrayList<>();
        for (UserHelper user : allUsers) {
            if (user.getUsername().startsWith(prefix) && !user.getUsername().equals(currentUsername)) {
                result.add(user);
            }
        }
        return result;
    }
}
