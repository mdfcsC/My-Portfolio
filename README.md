# My-Portfolio

Some minor achievements ...

## Minesweeper

*2025-02-19*  

After several days of intermittent "hard" work, with the assistance of Claude, ChatGPT, and DeepSeek, I finally dealt with this [lab assignment](https://github.com/cs-uob/software-tools/blob/main/15-js/lab/minesweeper.md).  

I added some functions beyond what was asked in the assignment, just for fun, or 因为闲得无聊...  

一开始选择不修改 html 和 css 的模版 —— 非常错误的决定！用 js 操控设计元素太令人头大了，特别是有些已经在 html 和 css 里了，有些又要额外添加。  

最花时间的不是逻辑，而是不知道该怎么操控元素，然后视觉设计的时候怎么都调整不对！（而且感觉 AI 对此的解决能力也明显比较弱）  

除了上学期的 C 的作业写了类似消消乐的程序（但是没有视觉化），算是真正意义上写的第一个小游戏，纪念一下！  

来，试试看，扫雷！[PLAY!](https://mdfcsc.github.io/My-Portfolio/minesweeper/minesweeper.html)  

## Simplified Database

*2025-03-14*  

去年12月考完后进入摆烂期，要么emo要么打游戏，结果2月初的时候游戏本坏了，这下不得不学习了···  

Java 课的第一个大作业，三周时间写一个数据库 —— 我错了，在最后一周才开始造火箭，还是边学边造😭  

有一些 [奇特的BNF语法规则](./cw-db-3_4/introduction/BNF.txt) ，说是为了简化我们的代码实现，但是多的时间用来理解它的语法树了 —— 上学期的 Architecture 教得还是简单了😓  

一些教师的授课资源： [任务布置](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/07-DB-Briefing.pdf) ， [方法指引](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/08-DB-Implementation.pdf) ， [工作手册](https://github.com/drslock/JAVA2024/tree/main/Weekly%20Workbooks/07%20DB%20assignment)  

2025年3月13日晚，经过将近一周的奋战，终于成功通过所有 BNF 需要的 SQL 命令的手动测试！反反复复写了好几版，目前这是 [第4版](./cw-db-3_4/) ，决定用 GitHub 记录一下！  
花了两天时间，在 Claude、ChatGPT、DeepSeek 的帮助下，才大致想明白整个项目的框架；（这时间上学期的C的作业都快写完了）  
希望AI查重能通过，虽然我逢人就说“当然用AI写”，以及我真的遇到问题就会问AI，但是我真的手动写了很多“垃圾”，以至于今天下午约到 Tutor 让他帮我检查问题，他直呼我的代码逻辑太混乱了（幸好一个多小时终于解决了！ [主要的问题](./cw-db-3_4/introduction/0313问题记录.pages) 是： `MySimpleCondition` 类中，如果本身是浮点数，是不能被 `Integer.parseInt()` 转为整形的，会直接导致错误抛出；以及一个 `Tokeniser` 中读取到数字时的循环逻辑问题。）  

上课时教授说要自己编写 JUnit 测试，好好好正在开始学习什么是 JUnit ···  

除了还需要编写 JUnit 测试外，目前其实还有一个潜在的问题：根据教授的要求，每张表的每行记录的id自动生成并且不可更改、复用，我目前的逻辑时读取一张新表时扫描其最大的 `id` 值，之后的新插入数据在此基础上递增，但有一个边缘隐患——如果删除了最大 `id` 值的记录，储存了文件，然后再读取，那么下一条插入的记录的 `id` 会重复使用这个最大值。  

有一个 [run-mvnw.bash](./cw-db-3_4/run-mvnw.bash) 文件，是 Claude 教我的方便在学校 MVB 的 Linux 电脑上用 Java17 运行程序的脚本。我之前在 Lab 的电脑上设置过一次 `JAVA_HOME`，但是这次去测代码发现又手动从 Java8 改到更新的版本···  
