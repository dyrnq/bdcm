//var editor1 = ace.edit("editor1");
//editor1.setTheme("ace/theme/twilight");
//editor1.session.setMode("ace/mode/yaml");
//editor1.setFontSize(16);
//editor1.setOptions({
//    minLines: 20,
//    maxLines: Infinity
//});
//editor1.resize();

function cleanData(d){
    console.log(d)
    $('#addForm1 input, #addForm1 select, #addForm1 textarea, #addForm1 checkbox').val('');
    if( d === true ){
        $("#div_id").hide();
        $('#addForm1 input[name="u"]').val("update");
    }else{
        $('#addForm1 input[name="u"]').val("add");
        $("#div_id").show();
    }
    layui.form.render('select');
    layui.form.render('checkbox');

//    console.log($('#addForm1 input[name="autoJob"]').val())
    $('#addForm1 input[name="autoJob"]').prop('checked', true);

}

function addLink(d) {
    let editBtn = '<button type="button" class="layui-btn layui-btn-normal layui-btn-xs" lay-event="edit">' + commonStr.edit + '</button>'
    let delBtn  = '<button type="button" class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">' + commonStr.del + '</button>'
    let logBtn = '<button type="button" class="layui-btn layui-btn-normal layui-btn-xs" onclick="viewLog(' + "\'" + d.id + "\'")">log</button>'
    return delBtn + '&nbsp;' + logBtn;
}

function viewLog(jobId) {
    window._currentLogJobId = jobId;
    var loadIdx = layer.load();
    var jobInfo = null;
    var jobLoad = $.ajax({
        type: 'GET',
        url: ctx + '/api/artJob/get',
        data: { id: jobId },
        dataType: 'json'
    });
    layui.jquery.ajax({
        type: 'GET',
        url: ctx + '/api/artJob/log/' + jobId,
        dataType: 'json'
    }).always(function(logRes) {
        $.when(jobLoad).done(function(jobRes) {
            layer.close(loadIdx);
            if (jobRes && jobRes.code == 200) {
                jobInfo = jobRes.data;
            }
            if (logRes && logRes.code == 200 && logRes.data && logRes.data.length > 0) {
                var logs = logRes.data;
                var html = '';
                html += '<div style="margin-bottom:10px"><button class="layui-btn layui-btn-xs layui-btn-normal" onclick="downloadLog()"><i class="layui-icon layui-icon-download-circle"></i> 下载日志</button></div>';
                html += '<div id="logContent">';
                for (var i = 0; i < logs.length; i++) {
                    html += '<pre style="margin:0;white-space:pre-wrap;word-break:break-all">' + escHtml(logs[i].log) + '</pre>';
                }
                html += '</div>';
                layer.open({
                    type: 1,
                    title: '日志 - Job #' + jobId,
                    area: ['900px', '600px'],
                    content: '<div style="padding:15px;max-height:550px;overflow:auto">' + html + '</div>',
                    shadeClose: true,
                    maxmin: true
                });
            } else {
                var summary = '';
                if (jobInfo) {
                    var statusMap = {0:'下载中',1:'完成',2:'异常'};
                    var statusText = statusMap[jobInfo.status] || jobInfo.status;
                    summary += '<div style="margin-bottom:15px;padding:10px;background:#f8f8f8;border-radius:4px">';
                    summary += '<div><b>状态：</b>' + statusText + '</div>';
                    if (jobInfo.progress) summary += '<div><b>进度：</b>' + jobInfo.progress + '</div>';
                    if (jobInfo.beginTime) summary += '<div><b>开始时间：</b>' + layui.util.toDateString(jobInfo.beginTime, 'yyyy-MM-dd HH:mm:ss') + '</div>';
                    if (jobInfo.endTime) summary += '<div><b>结束时间：</b>' + layui.util.toDateString(jobInfo.endTime, 'yyyy-MM-dd HH:mm:ss') + '</div>';
                    summary += '</div>';
                }
                layer.open({
                    type: 1,
                    title: '日志 - Job #' + jobId,
                    area: ['600px', '400px'],
                    content: '<div style="padding:15px">' + summary + '<div style="text-align:center;padding:30px;color:#999">暂无日志</div></div>',
                    shadeClose: true,
                    maxmin: true
                });
            }
        }).fail(function() {
            layer.close(loadIdx);
            layer.msg('获取日志失败');
        });
    });
}

function downloadLog() {
    var rawText = '';
    var pres = document.querySelectorAll('#logContent pre');
    for (var i = 0; i < pres.length; i++) {
        rawText += pres[i].textContent + '\n';
    }
    if (!rawText) { layer.msg('无日志内容'); return; }
    var blob = new Blob([rawText], { type: 'text/plain;charset=utf-8' });
    var link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'log-job-' + (window._currentLogJobId || 'unknown') + '.txt';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
}

function addStatus(d) {
    if (typeof d !== 'undefined' && d !== null && typeof d.status !== 'undefined' && d.status !== null) {
        if (d.status == 1) {
            return '完成';
        } else if (d.status == 0) {
            var prog = (typeof d.progress !== 'undefined' && d.progress !== null && d.progress !== '') ? ' ' + d.progress : '';
            return '下载中' + prog;
        } else if (d.status == 2) {
            return '异常';
        } else {
            return d.status;
        }
    } else {
        return '';
    }
}


layui.use(function(){

var layer = layui.layer;
var laypage = layui.laypage;
var table = layui.table;
var form = layui.form;
var upload = layui.upload;

var default_limit = localStorage.getItem('pageLimit');
if ('' == default_limit || null == default_limit || undefined == default_limit) {
    default_limit = cfg.pageLimit;
}

setInterval(function() {


  // 发送ajax请求
  layui.jquery.ajax({
    type: "GET",
    url: ctx + '/api/artJob/report',
    dataType: "json",
    success: function(data) {
      // 渲染数据到页面的div
      //console.log(data.data);
      try {
        //var jsonString = JSON.stringify(data.data, null, 2);
        var jsonString = data.data;
        layui.jquery("#yourDiv").html(jsonString);
        layui.jquery("#yourDiv").show();
      } catch (error) {
        layui.jquery("#yourDiv").hide();
      }
    }
  });
}, 3000);
setInterval(function() {
  // 发送ajax请求
  layui.jquery.ajax({
    type: "GET",
    url: ctx + '/api/artJob/threadPool',
    dataType: "json",
    success: function(data) {
      // 渲染数据到页面的div
      //console.log(data.data);
      try {
        //var jsonString = JSON.stringify(data.data, null, 2);
        var jsonString = data.data;
        layui.jquery("#threadDiv").html(jsonString);
        layui.jquery("#threadDiv").show();
      } catch (error) {
        layui.jquery("#threadDiv").hide();
      }
    }
  });
}, 3000);

form.on('switch(demo-checkbox-filter)', function(data){
    var elem = data.elem; // 获得 checkbox 原始 DOM 对象
    var checked = elem.checked; // 获得 checkbox 选中状态
    var value = elem.value; // 获得 checkbox 值
    var othis = data.othis; // 获得 checkbox 元素被替换后的 jQuery 对象
    if(checked){
        $(elem).val("1");
    }else{
        $(elem).val("0");
    }
    console.log('checked 状态: '+ elem.checked);
});

 form.on('submit(demo-table-search)', function(data){
     var field = data.field; // 获得表单字段
     // 执行搜索重载
     table.reload('demo', {
       page: {
         curr: 1 // 重新从第 1 页开始
       },
       where: field // 搜索的字段
     });
     return false; // 阻止默认 form 跳转
   });

$('#add').click(function(){
    cleanData(false);
    layer.open({
        type: 1,
        area: ['800px', '600px'],
        title: 'Add',
        content : $('#windowDiv'),
        anim: 'slideRight',
        shade: 0.6, // 遮罩透明度
        shadeClose: true, // 点击遮罩区域，关闭弹层
        maxmin: true, // 允许全屏最小化
        skin: 'layui-layer-win10'
    });

});

$('#addOver').click(function(){
    let u = $('#addForm1 input[name="u"]').val();
    var formData = $('#addForm1').serialize();
    //console.log(formData);
    //判断有没有勾选autoJob，此处比较罗嗦，有更好的方法欢迎留言
    var paramName = 'autoJob'; // 要判断的参数名
    var paramValue = '0'; // 要增加的参数值
    if (formData.indexOf(paramName + '=') === -1) {
      formData += '&' + paramName + '=' + paramValue;
    }
    $.ajax({
        type : 'POST',
        url: ctx + '/api/artJob/'+u,
        data : formData,
        dataType : 'json',
        success : function(data) {
            if(data.code=='200'){
                layer.closeAll();
                layer.msg(commonStr.success);
                table.reload('demo',{});
            } else {
                layer.msg(data.description);
            }
        },
        error : function() {
            layer.alert(commonStr.errorInfo);
        }
    });
});




    //执行一个 table 实例
    table.render({
        elem: '#demo'
        , height: 'full-30'
        , even: true
        , url: ctx + '/api/artJob' //数据接口
        , title: '用户表'
        , page: true //开启分页
        , limit: default_limit
        , limits: cfg.pageLimits
        , toolbar: '#toolbarDemo' //开启工具栏，此处显示默认图标，可以自定义模板，详见文档
        , defaultToolbar: ['filter', 'exports', 'print', { //自定义头部工具栏右侧图标。如无需自定义，去除该参数即可
            title: '提示'
            , layEvent: 'LAYTABLE_TIPS'
            , icon: 'layui-icon-tips'
        }]
        , totalRow: false //开启合计行
        , cols: [[ //表头
            {type: 'checkbox', fixed: 'left'}
            , {field: 'id', title: 'id', width: 200, sort: true, fixed: 'left', totalRowText: '合计：'}
            , {field: 'artName', title: 'name', width: 200}
            , {field: 'artUrl', title: 'url', width: 300, sort: true}
            , {field: 'status', title: 'status/progress', width: 160, templet: addStatus }
            , {field: 'beginTime', title: 'beginTime', sort: true, width: 300, templet: "<div>{{!d.beginTime?'-':layui.util.toDateString(d.beginTime, 'yyyy-MM-dd HH:mm:ss') }}</div>" }
            , {field: 'endTime', title: 'endTime', sort: true, width: 300, templet: "<div>{{!d.endTime?'-':layui.util.toDateString(d.endTime, 'yyyy-MM-dd HH:mm:ss') }}</div>" }
            , {field: 'upstream', title: 'operation', fixed: 'right', templet: addLink}

        ]]
        , done: function (res, curr, count){
            //如果是异步请求数据方式，res即为你接口返回的信息。
            //如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
            //console.log(res);
            //得到当前页码
            //console.log(curr);
            //得到数据总量
            //console.log(count);


            // 获取配置项
            var thisOptions = table.getOptions('demo');
            //console.log(thisOptions);
            localStorage.setItem('pageLimit', thisOptions.limit);


            if(res.data && res.data.length == 0){
                if(curr>1){
                    toPage=curr-1;
                    //console.log(toPage);
                    table.reload('demo',{page: {curr:toPage}});
                }
            }
        }
        , response: {
            statusCode: 200
        }
        , parseData: function (res) { //将原始数据解析成 table 组件所规定的数据
            return {
                "code": res.code, //解析接口状态
                "msg": res.description, //解析提示文本
                "count": res.total, //解析数据长度
                "data": res.data //解析数据列表
            };
        }
    });

//头部工具条监听事件
    table.on('toolbar(test)', function (obj) {
        var checkStatus = table.checkStatus(obj.config.id);
        switch (obj.event) {
//            case 'getCheckData':
//                var dataX = checkStatus.data;
//                layer.alert(JSON.stringify(dataX));
//                break;
            case 'deleteSelected':
                var dataX = checkStatus.data;
                var allId = [];
                if (dataX.length === 0) {
                    layer.msg(commonStr.pleaseSelect);
                } else {
                    layer.confirm(commonStr.confirmBatchDelete, function(index) {
                        for (let i = 0; i < dataX.length; i++) {
                            const val = dataX[i];
                            if(val.id == "1"){
                                continue;//在id=1的情况下跳过剩余循环，直接进入id=2的循环。
                            }else{
                                allId.push(val.id)
                            }
                        }

                        $.ajax({
                            url: ctx + '/api/artJob/del',
                            type:'post',
                            contentType: 'application/json',
                            data: JSON.stringify({id: allId}),
                            success:function (data,statusText) {
                                if(data.code=='200'){
                                    table.reload('demo',{});
                                    layer.msg(commonStr.delSuccess);
                                }else{
                                    layer.msg(data.description);
                                }
                            },
                            'error':function () {
                                layer.msg(commonStr.errorInfo);
                            }
                        });
                        layer.close(index);
                    });

                }
                break;
            case 'add':
                cleanData(false);
                layer.open({
                    type: 1,
                    area: ['800px', '600px'],
                    title: 'Add',
                    content : $('#windowDiv'),
                    anim: 'slideRight',
                    shade: 0.6, // 遮罩透明度
                    shadeClose: true, // 点击遮罩区域，关闭弹层
                    maxmin: true, // 允许全屏最小化
                    skin: 'layui-layer-win10'
                });
                break;


        }

})




//工具条事件
    table.on('tool(test)', function(obj){ //注：tool 是工具条事件名，test 是 table 原始容器的属性 lay-filter="对应的值"
        var data = obj.data; //获得当前行数据
        var layEvent = obj.event; //获得 lay-event 对应的值（也可以是表头的 event 参数对应的值）
        var tr = obj.tr; //获得当前行 tr 的 DOM 对象（如果有的话）


        if(layEvent === 'detail'){ //查看

        } else if(layEvent === 'del'){ //删除

                layer.confirm(commonStr.confirmDel, function(index){

                    $.ajax({
                    url: ctx + '/api/artJob/del',
                    type: 'post',
                    contentType: 'application/json',
                    data: JSON.stringify({id: [obj.data.id] }),
                    success:function (data,statusText) {
                         if(data.code=='200'){
                             obj.del();
                             layer.msg(commonStr.delSuccess);

                         }else{
                             layer.msg(data.description);
                         }
                    },
                    'error':function () {
                        layer.msg(commonStr.errorInfo);
                    }
                    });

                    layer.close(index);
                });

        } else if (layEvent === 'edit'){

                //console.log(obj.data.id);
                cleanData(true);
                $.ajax({
                    url: ctx + '/api/artJob/get',
                    type:'post',
                    contentType: 'application/json',
                    data:JSON.stringify({id:obj.data.id}),
                    success:function (data,statusText) {


                        if(data.code=='200'){

                            //批量回添数据
                            $('#addForm1 input, #addForm1 select, #addForm1 textarea, #addForm1 checkbox').each(function() {
                              var elementName = $(this).attr('name');
                              if (elementName in data.data) { // 判断对象中是否有该属性
                                $(this).val(data.data[elementName]); // 将属性对应的值填充到表单元素中
                              }
                              if ( elementName == 'autoJob') {
                                if (data.data[elementName] == 1) {
                                    $(this).prop('checked', true);
                                }else {
                                    $(this).prop('checked', false);
                                }
                              }
                            });
                            layui.form.render('select');
                            layui.form.render('checkbox');

                            layer.open({
                                type: 1,
                                area: ['800px', '600px'],
                                title: 'Edit',
                                content : $('#windowDiv'),
                                anim: 'slideRight',
                                shade: 0.6, // 遮罩透明度
                                shadeClose: true, // 点击遮罩区域，关闭弹层
                                maxmin: true, // 允许全屏最小化
                                skin: 'layui-layer-win10'
                            });
                        }else{
                             layer.msg(data.description);
                        }
                    },
                    'error':function () {
                        layer.msg(commonStr.errorInfo);
                    }
                });
            }


    });

  //监听Tab切换
  element.on('tab(demo)', function(data){
    layer.tips('切换了 '+ data.index +'：'+ this.innerHTML, this, {
      tips: 1
    });
  });})
