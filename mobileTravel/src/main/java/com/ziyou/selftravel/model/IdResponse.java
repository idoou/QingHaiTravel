/**
 * 
 */

package com.ziyou.selftravel.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author kuloud
 */
public class IdResponse {
    @SerializedName("id")
    public int id;

    @Override
    public String toString() {
        return "id:" + id;
    }
}
