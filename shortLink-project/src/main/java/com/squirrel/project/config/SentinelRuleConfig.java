package com.squirrel.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule createOrderRule = new FlowRule();
        createOrderRule.setResource("create_short-link");
        // 限流类型为 QPS（基于每秒访问次数进行限制）
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // 限流阈值为1，即每秒最多被访问一次
        createOrderRule.setCount(1);
        rules.add(createOrderRule);
        // 加载规则
        FlowRuleManager.loadRules(rules);
    }
}
