# IM_Video
1.对apk体积进行优化，使其减少百分之50
  一、so的打包时，摘除部分so不进行打包
  二、对部分图片进行格式转换png转wepp
  三、使用微信(andresguard)资源混淆压缩减少体积
2.对IM架构使用mvp，使代码高复用
3.使用IJKPlayer对流媒体的支持
4.集成bugly(bug收集反馈系统)和tinber(热修复框架)可以及时解决bug在不发版本的情况下
5.使用H5混合技术开发部分业务模块
6.使用AndroidPerformanceMonitor优化应用UI渲染及优化