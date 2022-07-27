package com.verification.code.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.verification.code.util.RedisUtil;
import com.verification.code.util.Result;
import com.verification.code.util.VerifyImageUtil;

/**
 * 说明：生成图片验证码
 *
 * @author:dev
 * @date 2022-07-01
 */
@RestController
public class VerifyImageController {

    private final int _failCounts = 3;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @throws
     * @Title: captureImg
     * @Description: TODO(抠图)
     * @param: @param httpServletResponse
     * @param: @param httpServletRequest
     * @param: @return
     * @param: @throws Exception
     * @return: Result
     */
    @SuppressWarnings("static-access")
    @RequestMapping("/capture/img")
    public Result captureImg() throws Exception {
        Map<String, Object> captureImgTemp = VerifyImageUtil.pictureTemplatesCut(
                new ClassPathResource(String.format(VerifyImageUtil.CLASSPATHURL_TEMPLATE, VerifyImageUtil.getTemplateIndex())).getFile(),
                new ClassPathResource(String.format(VerifyImageUtil.CLASSPATHURL_TARGET, VerifyImageUtil.getTargetIndex())).getFile(),
                VerifyImageUtil.CLASSPATHURL_TEMPLATE_EX_PNG,
                VerifyImageUtil.CLASSPATHURL_TARGET_EX_JPG
        );
        return new Result().success(captureImgTemp);
    }

    /**
     * @throws
     * @Title: captureCheckImgValidate
     * @Description: TODO(校验 ： 0成功 ， ! 1失败)
     * @param: @param param
     * @param: @param x
     * @param: @return
     * @return: Result
     */
    @SuppressWarnings("static-access")
    @RequestMapping("/capture/checkImgValidate")
    public Result captureCheckImgValidate(@RequestParam("acptureUuid") String acptureUuid,
                                          @RequestParam("offsetHorizontalX") Integer offsetHorizontalX,
                                          @RequestParam("otherParams") String otherParams
    ) {
        Object jsData = redisUtil.get(acptureUuid.trim());
        if (jsData == null) {
            return new Result().error("验证图片已失效，请刷新生成新图片！");
        }
        // 其他参数
        // System.out.println(otherParams);

        JSONObject jsonObject = JSONObject.parseObject(jsData.toString());
        int x = (int) jsonObject.get("dragLength");
        if (x >= offsetHorizontalX - 3 && x <= offsetHorizontalX + 3) {
            //校验成功，删除
            redisUtil.del(acptureUuid.trim());
            return new Result().success("手速快又准，恭喜你验证成功！");
        }


        // 失败次数判定
        int failCounts = (int) jsonObject.get("failCounts");
        if (failCounts < _failCounts) {
            failCounts++;
            jsonObject.put("failCounts", failCounts);
            redisUtil.set(acptureUuid.trim(), jsonObject.toString(), TimeUnit.MINUTES.toSeconds(VerifyImageUtil.MINUTES_10));
            return new Result().error("非常遗憾，验证失败了，再试一次吧！");
        } else {
            redisUtil.del(acptureUuid.trim());
            return new Result().error("验证图片已失效，请刷新生成新图片！");
        }
    }
}
