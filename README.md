# x-dev-tips
This repository contains the sample code for the 10x developer tips video series 

[Video #2: 10x程序员小Tips #2 - 你确定Spring Cloud OpenFeign，你用对了吗？](https://www.bilibili.com/video/BV1rf4y1t7eL/)
- OpenFeign(new version) doesn't really/fully support GZip response
- Need to customize circuit breakers not to count [400, 500) response as errors and eventually trigger circuit break


