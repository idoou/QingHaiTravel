/**
 * 
 */

package com.ziyou.selftravel.task;

/**
 * @author kuloud
 */
public interface TaskListener<T> {
    public void onResult(T result);

    public void onCancel(T result);
}
