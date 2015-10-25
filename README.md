# PureNote极简笔记
A pure,fast,concise Android note app.纯粹，快速，简洁的笔记app.
<br>
<br>~~See it in [Google Play](https://play.google.com/store/apps/details?id=com.duanze.gasst)~~暂停更新<br>
[小米应用商店](http://app.mi.com/detail/85433)
#一切皆为虚妄
-声明：虽然肉眼看上去跟这个项目[Notes](https://github.com/lguipeng/Notes)极为相似，但实现原理上其实完全不同，见码即知
<br>另外，虽然撞名了，但本项目其实很久很久以前就开始了……
<br>
<br>-本质上，这是本人学会新知识后进行实践的第一场所，也正是因为这点，迭代了非常多次，废弃代码占据了相当比例（一些代码风格迥异请见谅）
<br>
<br>对于Android新手来说，这份代码的价值有：
<br>-所涵盖知识点较为全面，完全读懂这份代码，国产入门教材可以丢了
<br>-纯原生写法，易读————相对于使用各种框架、lambda表达式、各种基类继承的项目来说（嘛，其实是自己太懒，没把这份代码往高大上的路数去整）


<br>-知识点细目：
<br>+[stormzhang](http://www.stormzhang.com/)分享的Toolbar适配方案使用实例
<br>+[Notes](https://github.com/lguipeng/Notes)中的Material Design风格，只用v7包就可轻松实现哦
<br>+Loader加载器的使用，这个自带观察者回调的组件简直碉堡，有了它，再也不用写Observer了，如果Google能把Dialer中那套老得掉渣的数据加载换成这套，我的工作会轻松数倍吧
<br>+仿[易写](http://app.mi.com/detail/97233?ref=search)的监听软键盘弹出消隐Toolbar效果
<br>+RecyclerView轻松实现瀑布流，令RecyclerView.Adapter使用Cursor数据集以方便匹配Loader
<br>+AlarmManager全操作
<br>+自定义Notification，RemoteViews的使用
<br>+如何手搓一个Material风格的Preference设置页面，其优点是可自定义空间大（真相是，我在刚开始做设置页时，还未知晓存在PreferenceActivity这种东西...）
<br>+更多细节还请自行[下载](http://app.mi.com/detail/85433)（可能还未过审）感受

<br>构建环境：
<br>-Gradle 1.3.0
<br>-Sdk 22
<br>-buildtool 23.0.1

#效果展示
![](http://github.com/duanze/PureNote/app_asset/Screenshot_2015-10-25-21-01-44.png)
<!--
#更新历史
v2.2.1<br>
渐趋完善。



v2.1.8<br>
新功能：回收站<br>
现在，可以通过长按来重命名笔记分组<br>
修改了通知栏RemoteViews右侧按键为文字，采用了新的文本解析<br>
增加兼容性，4.0也可使用<br>
<br>
v2.1.7<br>
全面改版，全新感受<br>
少即是多，进一步精化应用，细心打磨每一分细节<br>
重构代码，全面Loader化，进一步提升性能及流畅度，即使内存吃紧也毫无压力<br>
增加代码许可，向无私的开源者致敬<br>
更多功能，即将到来……<br>
<br>
v2.1.6<br>
现在，可以通过长按笔记来进行批量操作<br>
调整阅读编辑模式<br>
取消笔记字数限制<br>
重构代码，增加性能及流畅性<br>
fix some bugs<br>
<br>
v2.1.2<br>
新增：独创的闪电摘录功能，轻松记下或精美或深凝的价值文字<br>
新增：通知栏快写模式，不让任何一丝灵感流失<br>
fix bugs<br>
<br>
v2.1.1<br>
新增api19以上的沉浸式适配，忘记密码处理方案<br>
笔记分组功能的优化<br>
修正了几个bug<br>

<br>
v2.1.0<br>
新增：密码保护<br>
优化代码提升性能<br>
由于旧有代码不够健壮，彩格视图暂且取消，将在之后重制<br>
<br>
v2.0.9<br>
1.修正重复删除通知栏提醒的bug<br>
2.修正了一个4.0版本下的适配问题<br>
<br>
v2.0.8<br>
1.修正了一个初始程序的崩溃问题<br>
2.修正列表模式下时间戳12月缩写错误<br>
<br>
v2.0.7<br>
1.重新设计笔记分组<br>
2.解决了一个彩格视图模式下的bug<br>
3.低版本适配<br>
<br>
v2.0.5<br>
1.重新制件笔记分组为侧滑抽屉<br>
2.优化代码提升性能<br>
<br>
v2.0.4<br>
1.新功能，笔记分组，让一切井井有条！<br>
2.解决了一个彩格视图模式下的严重bug<br>
3.多处细节优化<br>
<br>
v2.0.3<br>
1.全新彩色标签功能<br>
2.优化代码提升性能<br>
3.ui美化<br>
<br>
v2.0.2<br>
修正定时提醒通知栏显示问题<br>
<br>
v2.0<br>
增加EverNote云同步支持<br>
<br>
v1.9<br>
1.增强英化版本<br>
2.重构代码，提升性能<br>
<br>
v1.8<br>
重大更新！请卸载旧版后安装<br>
1.现在，你可以自定义Note色彩<br>
2.修改切换动画，更快更顺畅<br>
3.定时提醒修改为持续响铃<br>
4.多重UI优化，全新感受<br>
<br>
v1.7<br>
1.修正列表模式滑动钮删除时错误取消定时提醒的bug<br>
2.优化代码提升运行速度<br>
<br>
v1.6<br>
1.列表模式添加滑动菜单<br>
2.彩格模式添加DONE Button，调整色彩效果<br>
<br>
v1.5<br>
1.为天气同步添加了自动定位<br>
2.新的滑动效果<br>
<br>
v1.4<br>
1.多项优化，引入绚彩fabButton<br>
2.调整彩格概率，删去巧克力色块，更改为“透明”<br>
3.重写定时提醒模块<br>
<br>
v1.3<br>
1.修正定时提醒无法取消<br>
2.更改操作细节<br>
<br>
v1.2<br>
1.更改UI呈现，优化多重事件响应<br>
2.重写天气同步模块，多处icon重制<br>
3.修正定时提醒失效等若干bug<br>
-->
#Special Thanks
[PixelLove](http://www.pixellove.com/)
<br>daimajia,[EverMemo](https://github.com/daimajia/EverMemo)
<br>flavienlaurent，[DateTimePicker](https://github.com/flavienlaurent/datetimepicker)
<br>FaizMalkani，[FloatingActionButton](https://github.com/ FaizMalkani/FloatingActionButton)
<br>baoyongzhang，[SwipeMenuListView](https://github.com/baoyongzhang/SwipeMenuListView)
<br>lguipeng，[Notes](https://github.com/lguipeng/Notes)

#关于我
某大2015届软工本科，现居上海，就职于手机行业某公司，通讯组Telephony应用层码农，入职即跟进高压X为项目（这一点跟早前X泰不幸英年高逝的清华哥雷同），包办Dialer及其他各种杂活，与Google源码、XXUI、Mtk feature Plugin亲密肉搏，目前最大心愿为每天能按时上下班-_-||
<br>mail:ggigfe347@gmail.com
