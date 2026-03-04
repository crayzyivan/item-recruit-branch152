package com.item.framework.config;

import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;


/**
 * 招聘状态流转  状态机
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  16:28
 */
@Configuration
@EnableStateMachineFactory
public class JobApplyStateMachineConfig extends StateMachineConfigurerAdapter<JobApplyStatus, JobApplyStatusEvent> {

    /**
     * 招聘流程状态
     * @param states
     * @throws Exception
     */
    @Override
    public void configure(StateMachineStateConfigurer<JobApplyStatus, JobApplyStatusEvent> states) throws Exception {
        states.withStates().initial(JobApplyStatus.SUBMITTED)//初始状态 已提交
                .state(JobApplyStatus.SCREENED) //AI筛选
                .state(JobApplyStatus.VETTED)  //AI面试
                .state(JobApplyStatus.REVIEW) //人工审核
                .state(JobApplyStatus.MANUAL_REVIEW)
                .state(JobApplyStatus.READY)  //就绪
                .state(JobApplyStatus.BACKGROUND) //背景审核
                .end(JobApplyStatus.DENIED); //拒绝
    }

    /**
     * 招聘流程状态流转
     * @param transitions
     * @throws Exception
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<JobApplyStatus, JobApplyStatusEvent> transitions) throws Exception {
        transitions
                .withExternal().source(JobApplyStatus.SUBMITTED).target(JobApplyStatus.SUBMITTED).event(JobApplyStatusEvent.SUBMIT).and()
                //AI筛选后通过
                .withExternal().source(JobApplyStatus.SUBMITTED).target(JobApplyStatus.SCREENED).event(JobApplyStatusEvent.SCREEN).and()
                //进行AI面试
                .withExternal().source(JobApplyStatus.SCREENED).target(JobApplyStatus.VETTED).event(JobApplyStatusEvent.VETTED).and()
                //AI面试后通过
                .withExternal().source(JobApplyStatus.VETTED).target(JobApplyStatus.REVIEW).event(JobApplyStatusEvent.AI_PASS).and()
                //自动流程 AI通过到就绪
                .withExternal().source(JobApplyStatus.VETTED).target(JobApplyStatus.READY).event(JobApplyStatusEvent.AUTO_AI_TO_READY).and()
                //自动流程 AI不通过到人工审核
                .withExternal().source(JobApplyStatus.VETTED).target(JobApplyStatus.MANUAL_REVIEW).event(JobApplyStatusEvent.AUTO_AI_TO_REVIEW).and()
                //自动流程 人工审核界面-通过操作
                .withExternal().source(JobApplyStatus.MANUAL_REVIEW).target(JobApplyStatus.READY).event(JobApplyStatusEvent.AUTO_REVIEW_PASS).and()
                //人工审核通过
                .withExternal().source(JobApplyStatus.REVIEW).target(JobApplyStatus.READY).event(JobApplyStatusEvent.REVIEW_PASS).and()
                //进行背景调查
                .withExternal().source(JobApplyStatus.READY).target(JobApplyStatus.BACKGROUND).event(JobApplyStatusEvent.BACKGROUND_CHECK).and()
                //拒绝操作
                .withExternal().source(JobApplyStatus.SUBMITTED).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT).and()
                .withExternal().source(JobApplyStatus.SCREENED).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT).and()
                .withExternal().source(JobApplyStatus.VETTED).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT).and()
                .withExternal().source(JobApplyStatus.REVIEW).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT).and()
                .withExternal().source(JobApplyStatus.READY).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT).and()
                //自动流程 人工审核不通过-》拒绝
                .withExternal().source(JobApplyStatus.MANUAL_REVIEW).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT).and()
                .withExternal().source(JobApplyStatus.BACKGROUND).target(JobApplyStatus.DENIED).event(JobApplyStatusEvent.REJECT);

    }
}