package com.item.framework.constant;

import java.io.Serializable;

public class ResumeAiScoreWeight  implements Serializable {
    public static final int JOB_TITLE = 10;
    public static final int SKILLS = 25;
    /**
     *岗位要求
     */
    public static final int REQUIREMENT = 25;
    /**
     * 职责 过往经历匹配度
     */
    public static final int RESPONSIBILITY = 15;
    /**
     * 经验  技能经验匹配度
     */
    public static final int EXPERIENCE = 15;
    public static final int LOCATION = 5;
    public static final int MINIMUM_SALARY = 0;
    public static final int MAXIMUM_SALARY = 5;
    // 总和100
}
