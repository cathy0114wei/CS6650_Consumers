package dto;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This is for recording the user info. When users swipe right, the like will increase.
 */
public class UserInfo {
    private final int userId;
    //swipe right
    private int likeNum;
    //swipe left
    private int dislikeNum;
    private Queue<Integer> likeQueue;
    private int queueCapacity;

    public UserInfo(int userId, int queueCapacity) {
        this.userId = userId;
        this.likeNum = 0;
        this.dislikeNum = 0;
        this.likeQueue = new LinkedList<>();
        this.queueCapacity = queueCapacity;
    }

    public int getUserId() {
        return userId;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public int getDislikeNum() {
        return dislikeNum;
    }

    public void addDislikeNum(){
        this.dislikeNum++;
    }

    /**
     * @param swipeeId
     * this will firstly increase the likeNum, then check if the queue size meets the capacity, if so, poll one out.
     * But always insert the new swipee into the queue.
     */
    public void addLikeUsers(int swipeeId){
        this.likeNum++;
        if(queueCapacity >= likeQueue.size()){
            likeQueue.poll();
        }
        likeQueue.offer(swipeeId);
    }
    public Queue<Integer> getLikeQueue() {
        return likeQueue;
    }
}
