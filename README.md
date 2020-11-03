# es-jd
京东搜索引擎案例


##  我之所以前端页面开始出不来犯了两个错。


1、
  忘了 加id app
```html
<body class="pg">
<div class="page" id="app">

```


2、
```js
        // 对接接口
                axios.get('search/' + keyword + "/1/10").then(response => {
                    console.log(response);
                    this.results = response.data; // 绑定数据
```
这里的this.results我写成了this.result少了个s


3、<font color=red>net::ERR_ ABORTED 404</font>

https://blog.csdn.net/qq_21040559/article/details/109472409

4、<font color=red>Could not load content for http://localhost:1111/js/axios.min.map: HTTP error: status code 404</font>
https://blog.csdn.net/qq_21040559/article/details/109472612
