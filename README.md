# PureNote
A pure,fast,concise Android note app.纯粹，快速，简洁的笔记app.
<br>
<br>See it in [Google Play](https://play.google.com/store/apps/details?id=com.duanze.gasst)<br>
[小米应用商店](http://app.mi.com/detail/85433)
#一切皆为虚妄
-声明：虽然肉眼看上去跟这个项目[Notes](https://github.com/lguipeng/Notes)极为相似，但实现原理上其实完全不同，见码即知
<br>另外，虽然撞名了，但本项目其实很久很久以前就开始了……
<br>

<br>-知识点细目：
<br>+[stormzhang](http://www.stormzhang.com/)分享的Toolbar适配方案使用实例
<br>+[Notes](https://github.com/lguipeng/Notes)中的Material Design风格，只用v7包就可轻松实现哦
<br>+Loader加载器的使用，这个自带观察者回调的组件简直碉堡，有了它，再也不用写Observer了，如果Google能把Dialer中那套老得掉渣的数据加载换成这套，我的工作会轻松数倍吧
<br>+仿[易写](http://app.mi.com/detail/97233?ref=search)的监听软键盘弹出消隐Toolbar效果
<br>![](https://github.com/duanze/PureNote/blob/master/app_asset/pure.gif)
<br>
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
![](https://github.com/duanze/PureNote/blob/master/app_asset/Screenshot_2015-10-25-21-01-44.png)
![](https://github.com/duanze/PureNote/blob/master/app_asset/Screenshot_2015-10-25-21-02-10.png)
![](https://github.com/duanze/PureNote/blob/master/app_asset/Screenshot_2015-10-25-21-15-00.png)
![](https://github.com/duanze/PureNote/blob/master/app_asset/Screenshot_2015-10-25-21-15-16.png)

#Special Thanks
[PixelLove](http://www.pixellove.com/)
<br>daimajia,[EverMemo](https://github.com/daimajia/EverMemo)
<br>flavienlaurent，[DateTimePicker](https://github.com/flavienlaurent/datetimepicker)
<br>FaizMalkani，[FloatingActionButton](https://github.com/ FaizMalkani/FloatingActionButton)
<br>baoyongzhang，[SwipeMenuListView](https://github.com/baoyongzhang/SwipeMenuListView)
<br>lguipeng，[Notes](https://github.com/lguipeng/Notes)

#关于我
某大2015届软工本科，现居上海，就职于手机行业某公司，通讯组Telephony应用层码农，与Google源码、XXUI、Mtk feature Plugin亲密肉搏，目前最大心愿为每天能按时上下班-_-||
<br>mail:ggigfe347@gmail.com

#License
<br>Copyright 2015 Duanze
<br>
<br>   Licensed under the Apache License, Version 2.0 (the "License");
<br>   you may not use this file except in compliance with the License.
<br>   You may obtain a copy of the License at
<br>
<br>       http://www.apache.org/licenses/LICENSE-2.0
<br>
<br>   Unless required by applicable law or agreed to in writing, software
<br>   distributed under the License is distributed on an "AS IS" BASIS,
<br>   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
<br>   See the License for the specific language governing permissions and
<br>   limitations under the License.