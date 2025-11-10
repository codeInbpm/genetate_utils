package com.example.changl;

import java.util.*;

/**
 * <p>双色球随机模拟</p>
 * @author yuanfeng.wang
 * @since 2023/8/29
 */
public class CaiPiao {

    private static Random random = new Random();

    /**
     * 开奖的红球
     */
    private static Set<Integer> winningRedBalls;

    /**
     * 开奖的蓝球
     */
    private static int winningBlueBall;

    // 静态块初始化一组开奖号码
    static {
        // 篮球 01-16
        winningBlueBall = random.nextInt(16) + 1;

        // 红球 01-33生成6个
        winningRedBalls = new HashSet<>();
        while (winningRedBalls.size() < 6) {
            int num = random.nextInt(33) + 1;
            winningRedBalls.add(num);
        }
    }

    public static void main(String[] args) {
        play(500_0000);
    }

    /**
     *
     * @param num 运行一次程序只开一次奖，此参数表示总共购买多少注
     */
    public static void play(int num) {
        System.out.println("\n本期开奖号码：");
        System.out.println("红球：" + winningRedBalls + " 篮球：" + winningBlueBall);
        for (int i = 0; i < num; i++) {
            playOnce();
        }
    }

    private static void playOnce() {
        Set<Integer> userRedBalls = getUserSelectedRedBalls();
        int userBlueBall = getUserSelectedBlueBall();

        int redBallMatch = countMatchingBalls(userRedBalls, winningRedBalls);
        boolean blueBallMatch = (userBlueBall == winningBlueBall);

        if (redBallMatch == 6 && blueBallMatch) {
            System.out.println("\n恭喜你中了一等奖！");
            System.out.println("玩家购买的号码：");
            System.out.println("红球：" + userRedBalls + " 蓝球：" + userBlueBall);
        } else if (redBallMatch == 6) {
            System.out.println("\n恭喜你中了二等奖！");
        } else if (redBallMatch == 5 && blueBallMatch) {
//            System.out.println("\n恭喜你中了三等奖！");
        } else if (redBallMatch == 5 || (redBallMatch == 4 && blueBallMatch)) {
//            System.out.println("\n恭喜你中了四等奖！");
        } else if (redBallMatch == 4 || (redBallMatch == 3 && blueBallMatch)) {
//            System.out.println("\n恭喜你中了五等奖！");
        } else if (blueBallMatch) {
//            System.out.println("\n恭喜你中了最小奖！");
        } else {
            //没中奖，不打印记录
        }
    }

    /**
     * 返回玩家选择的6个红球,范围1-33，不重复
     */
    private static Set<Integer> getUserSelectedRedBalls() {
        Set<Integer> userRedBalls = new HashSet<>();
        while (userRedBalls.size() < 6) {
            int num = random.nextInt(33) + 1;
            userRedBalls.add(num);
        }
        return userRedBalls;
    }

    /**
     * 玩家选择的1个蓝球,范围1-16
     */
    private static int getUserSelectedBlueBall() {
        return random.nextInt(16) + 1;
    }

    /**
     * 匹配中了几个红球
     * @return 中红球个数
     */
    private static int countMatchingBalls(Set<Integer> userBalls, Set<Integer> winningBalls) {
        int count = 0;
        for (int ball : userBalls) {
            if (winningBalls.contains(ball)) {
                count++;
            }
        }
        return count;
    }

}