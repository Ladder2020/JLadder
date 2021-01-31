package com.jladder.data;
import com.jladder.hub.WebHub;
import com.jladder.lang.Json;
import com.jladder.lang.Regex;
import com.jladder.lang.Times;

import java.util.*;

/***
 * 请求返回结果类
 */
public class RequestResult{

        /***
         * 初始化开始时间
         */
        private transient long _dtstart = 0;
        /***
         * 是否处理成功
         */
        public boolean Success;


        /***
         * 状态码
         */
        public int Code;

        /***
         * 消息正文
         */
        public String Message;
        /// <summary>
        /// 资源索引
        /// </summary>
        public String Rel;
        /// <summary>
        /// 返回结果
        /// </summary>
        public Object Result;

        /// <summary>
        /// 返回时间
        /// </summary>
        public long ReTime = Times.getTS();
        /// <summary>
        /// 处理时长
        /// </summary>
        public String duration  = "0ms";

        /// <summary>
        /// 节点标识
        /// </summary>

        public String watchPoint = WebHub.SiteName;
        /// <summary>
        /// 未定义
        /// </summary>
        public String datatype = "undefined";

        private Map<String, String> config = null;

        /// <summary>
        /// 基本构造
        /// </summary>
        public RequestResult()
        {
            _dtstart = Times.getTS();
            Code = 200;
            Message = AjaxResultCode.getMessage(200);
        }
        /// <summary>
        /// 基本构造
        /// </summary>
        /// <param name="result">返回情况</param>
        public RequestResult(Object result)
        {
            _dtstart =Times.getTS();;
            if (result == null)
            {
                this.Code = 400;
                Message = AjaxResultCode.Undfind.name();
                return;
            }
            else if (result instanceof Integer)
            {
                this.Code = (int)result;
                if (Code != 200) Message = AjaxResultCode.getMessage(Code);
                else this.Message = "";
                return;
            }
            else if (result instanceof String)
            {
                if (Regex.isMatch(result.toString(), "^\\d+$"))
                {
                    this.Code = Integer.valueOf(result.toString());
                    return;
                }
            }
             else if (result instanceof AjaxResult)
             {
                 AjaxResult ret = (AjaxResult)result;
                 Code = ret.statusCode;
                 Message = ret.message;
                 Result = ret.data;
                 Rel = ret.rel;
                 duration = ret.duration;
                 datatype = ret.datatype;
                 return;
             }
            else if (result instanceof Boolean)
            {
                boolean re = (Boolean)result;
                if (re)
                {
                    Code = 200;
                    return;
                }
                else
                {
                    Code = 500;
                    Message = AjaxResultCode.getMessage(500);
                    return;
                }
            }
            Code = 200;
            Message = "操作成功";
            Result = result;
        }

        /// <summary>
        /// 基本构造
        /// </summary>
        /// <param name="code">状态码</param>
        /// <param name="message">消息文本</param>
        public RequestResult(int code, String message)
        {
            _dtstart = Times.getTS();
            Message = message;
            Code = code;
        }

        /// <summary>
        /// 设置
        /// </summary>
        /// <param name="code">状态码</param>
        /// <param name="msg">消息文本</param>
        /// <returns></returns>
        public RequestResult set(int code, String msg)
        {
            _dtstart = Times.getTS();
            Code = code;
            Message = msg;
            return this;
        }
    /***
     * 设置消息文本
     * @param message 消息文本
     * @return
     */
        public RequestResult setMessage(String message)
        {
            _dtstart = Times.getTS();
            Message = message;
            return this;
        }

        /// <summary>
        /// 设置处理时长
        /// </summary>
        /// <param name="startTime">开始时间</param>
        /// <returns></returns>
        public RequestResult SetDuration(Date startTime)
        {
            long time = new Date().getTime() - startTime.getTime();
            duration = time+"ms";
            return this;
        }
        /// <summary>
        /// 设置处理时长
        /// </summary>
        /// <param name="duration">开始时间</param>
        /// <returns></returns>
        public RequestResult setDuration(String duration)
        {
            _dtstart = Times.getTS();
            duration = duration;
            return this;
        }
        /// <summary>
        /// 设置返回数据
        /// </summary>
        /// <param name="data">数据</param>
        /// <returns></returns>
        public RequestResult setData(Object data)
        {
            _dtstart = Times.getTS();
            Result = data;
            return this;
        }
        /// <summary>
        /// 推入数据
        /// </summary>
        /// <param name="data">数据</param>
        /// <returns></returns>
        public RequestResult pushData(Object data)
        {
            _dtstart = Times.getTS();
            if (Result == null)
            {
                Result = new ArrayList<Object>();
            }
            if (data instanceof List)
            {
                ((java.util.List<Object>)this.Result).add(data);
                return this;
            }
            List<Object> li = new ArrayList<Object>();
            li.add(this.Result);
            li.add(data);
            this.Result = li;
            return this;
        }
        /// <summary>
        /// 设置资源索引
        /// </summary>
        /// <param name="rel"></param>
        /// <returns></returns>
        public RequestResult setRel(String rel)
        {
            _dtstart = Times.getTS();
            Rel = rel;
            return this;
        }
        /// <summary>
        /// 设置映射节点
        /// </summary>
        /// <param name="node"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public RequestResult setMapping(String node, String name)
        {
            _dtstart = Times.getTS();
            if (config == null) config = new HashMap<>();
            config.put(node, name);
            return this;
        }
        /// <summary>
        /// 文本格式化
        /// </summary>
        /// <returns></returns>
        public String ToString()
        {
            if (config == null) return Json.toJson(this);
            else
            {
                Record record = Record.parse(this);
                config.forEach((x,y)->record.re(x,y));
                return Json.toJson(record);
            }
        }
    }
