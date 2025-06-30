layui.use(function(){

var layer = layui.layer;
var laypage = layui.laypage;
var table = layui.table;
var form = layui.form;
var upload = layui.upload;



form.on('submit(demo2)', function(_data){


    var field = _data.field; // 获得表单字段
    var params = Object.keys(field).map(key => `${key}=${encodeURIComponent(field[key])}`).join('&');
    url= ctx + '/api/maven/url';
    $.ajax({
      type: 'POST',
      data: params,
      contentType: 'application/x-www-form-urlencoded',
      url: url,
      dataType: 'json',
      success: function(data) {
            console.log(data.data);
            $("#url").html("");

            for (const i in data.data.original) {
                $('#url').append(data.data.original[i]);
                $('#url').append('<br/>');
            }
            $('#url').append('#####################<br/>');
            for (const i in data.data.huawei) {
                $('#url').append(data.data.huawei[i]);
                $('#url').append('<br/>');
            }
            $('#url').append('#####################<br/>');
            for (const i in data.data.aliyun) {
                $('#url').append(data.data.aliyun[i]);
                $('#url').append('<br/>');
            }

            $('#url').append('#####################<br/>');
            for (const i in data.data.local) {
                $('#url').append(data.data.local[i]);
                $('#url').append('<br/>');
            }

            $('#url').append('#####################<br/>');
            for (const i in data.data.cmd) {
                $('#url').append(data.data.cmd[i]);
                $('#url').append('<br/>');
            }



      },
      error: function(xhr, status, error) {
            // 在此处输入 layer 的任意代码
            layer.open({
              type: 1, // page 层类型
              area: ['500px', '300px'],
              title: '异常',
              shade: 0.6, // 遮罩透明度
              shadeClose: true, // 点击遮罩区域，关闭弹层
              maxmin: true, // 允许全屏最小化
              anim: 0, // 0-6 的动画形式，-1 不开启
              content: error
            });
      }
    });

return false; // 阻止默认 form 跳转
});


});
