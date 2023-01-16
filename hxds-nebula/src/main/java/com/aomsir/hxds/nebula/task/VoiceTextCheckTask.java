package com.aomsir.hxds.nebula.task;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.nebula.db.dao.OrderMonitoringDao;
import com.aomsir.hxds.nebula.db.dao.OrderVoiceTextDao;
import com.aomsir.hxds.nebula.db.pojo.OrderMonitoringEntity;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ciModel.auditing.AuditingJobsDetail;
import com.qcloud.cos.model.ciModel.auditing.SectionInfo;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingResponse;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tms.v20201229.TmsClient;
import com.tencentcloudapi.tms.v20201229.models.TextModerationRequest;
import com.tencentcloudapi.tms.v20201229.models.TextModerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.swing.plaf.ListUI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class VoiceTextCheckTask {
    @Value("${tencent.cloud.appId}")
    private String appId;

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.bucket-public}")
    private String bucketPublic;

    @Resource
    private OrderVoiceTextDao orderVoiceTextDao;

    @Resource
    private OrderMonitoringDao orderMonitoringDao;

    @Async
    @Transactional
    public void checkText(long orderId, String content, String uuid) {

        String label = "Normal"; //审核结果
        String suggestion = "Pass"; //后续建议
        
        //后续建议模板
        HashMap<String, String> template = new HashMap() {{
            put("0", "Pass");
            put("1", "Block");
            put("2", "Review");
        }};

        if (StrUtil.isNotBlank(content)) {
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            Region region = new Region("ap-beijing");
            ClientConfig clientConfig = new ClientConfig(region);
            COSClient client = new COSClient(cred, clientConfig);
            
            TextAuditingRequest request = new TextAuditingRequest();
            request.setBucketName(bucketPublic);
            request.getInput().setContent(Base64.encode(content));
            request.getConf().setDetectType("all");

            TextAuditingResponse response = client.createAuditingTextJobs(request);
            AuditingJobsDetail detail = response.getJobsDetail();
            String state = detail.getState();
            ArrayList keywords = new ArrayList();
            if ("Success".equals(state)) {
                label = detail.getLabel(); //检测结果
                String result = detail.getResult(); //后续建议
                suggestion = template.get(result);
                List<SectionInfo> list = detail.getSectionList();   //违规关键词

                for (SectionInfo info : list) {
                    String keywords_1 = info.getPornInfo().getKeywords();
                    String keywords_2 = info.getIllegalInfo().getKeywords();
                    String keywords_3 = info.getAbuseInfo().getKeywords();
                    if (keywords_1.length() > 0) {
                        List temp = Arrays.asList(keywords_1.split(","));
                        keywords.addAll(temp);
                    }
                    if (keywords_2.length() > 0) {
                        List temp = Arrays.asList(keywords_2.split(","));
                        keywords.addAll(temp);
                    }
                    if (keywords_3.length() > 0) {
                        List temp = Arrays.asList(keywords_3.split(","));
                        keywords.addAll(temp);
                    }
                }
            }
            Long id = orderVoiceTextDao.searchIdByUuid(uuid);
            if (id == null) {
                throw new HxdsException("没有找到代驾语音文本记录");
            }
            HashMap param = new HashMap();
            param.put("id", id);
            param.put("label", label);
            param.put("suggestion", suggestion);
            param.put("keywords", ArrayUtil.join(keywords.toArray(), ","));
            
            //更新数据表中该文本的审核结果
            int rows = orderVoiceTextDao.updateCheckResult(param);
            if (rows != 1) {
                throw new HxdsException("更新内容检查结果失败");
            }
            log.debug("更新内容验证成功");
            
            //查询该订单有多少个录音文本和需要人工审核的文本
            HashMap map = orderMonitoringDao.searchOrderRecordsAndReviews(orderId);
            
            id = MapUtil.getLong(map, "id");
            Integer records = MapUtil.getInt(map, "records");
            Integer reviews = MapUtil.getInt(map, "reviews");
            OrderMonitoringEntity entity = new OrderMonitoringEntity();
            entity.setId(id);
            entity.setOrderId(orderId);
            entity.setRecords(records + 1);
            if (suggestion.equals("Review")) {
                entity.setReviews(reviews + 1);
            }
            if (suggestion.equals("Block")) {
                entity.setSafety("danger");
            }
            
            //更新order_monitoring表中的记录
            orderMonitoringDao.updateOrderMonitoring(entity);

        }
    }
}
