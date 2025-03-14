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

*2025-03-13*  

去年12月考完后进入摆烂期，要么emo要么打游戏，结果2月初的时候游戏本坏了，这下不得不学习了···  

Java 课的第一个大作业，三周时间写一个数据库 —— 我错了，在最后一周才开始造火箭，还是边学边造😭  

有一些 [奇特的BNF语法规则](./SimplifiedDatabase/cw-db-3_4/introduction/BNF.txt) ，说是为了简化我们的代码实现，但是多的时间用来理解它的语法树了 —— 上学期的 Architecture 教得还是简单了😓  

一些教师的授课资源： [任务布置](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/07-DB-Briefing.pdf) ， [方法指引](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/08-DB-Implementation.pdf) ， [工作手册](https://github.com/drslock/JAVA2024/tree/main/Weekly%20Workbooks/07%20DB%20assignment)  

2025年3月13日晚，经过将近一周的奋战，终于成功通过所有 BNF 需要的 SQL 命令的手动测试！反反复复写了好几版，目前这是 [第4版](./SimplifiedDatabase/cw-db-3_4/) ，决定用 GitHub 记录一下！  
花了两天时间，在 Claude、ChatGPT、DeepSeek 的帮助下，才大致想明白整个项目的框架；（这时间上学期的C的作业都快写完了）  
希望AI查重能通过，虽然我逢人就说“当然用 AI 写”，以及我真的遇到问题就会问 AI ，但是我真的手动写了很多“垃圾”，以至于今天下午约到 Tutor 让他帮我检查问题，他直呼我的代码逻辑太混乱了（幸好一个多小时终于解决了！ [主要的问题](./SimplifiedDatabase/cw-db-3_4/introduction/0313问题记录.txt) 是： `MySimpleCondition` 类中，如果本身是浮点数，是不能被 `Integer.parseInt()` 转为整形的，会直接导致错误抛出；以及一个 `Tokeniser` 中读取到数字时的循环逻辑问题。）  

上课时教授说要自己编写 JUnit 测试，好好好正在开始学习什么是 JUnit ···  

除了还需要编写 JUnit 测试外，目前其实还有一个潜在的问题：根据教授的要求，每张表的每行记录的id自动生成并且不可更改、复用，我目前的逻辑时读取一张新表时扫描其最大的 `id` 值，之后的新插入数据在此基础上递增，但有一个边缘隐患——如果删除了最大 `id` 值的记录，储存了文件，然后再读取，那么下一条插入的记录的 `id` 会重复使用这个最大值。  

有一个 [run-mvnw.bash](./SimplifiedDatabase/cw-db-3_4/run-mvnw.bash) 文件，是 Claude 教我的方便在学校 MVB 的 Linux 电脑上用 Java17 运行程序的脚本。我之前在 Lab 的电脑上设置过一次 `JAVA_HOME`，但是这次去测代码发现又手动从 Java8 改到更新的版本···  

*2025-03-14*  

今天中午提交了最终作业。  

1. [最终版](./SimplifiedDatabase/cw-db-3_5/) 修改了对 SQL query 语句的要求，之前到代码运行结尾是 EOF 或者分号，现在要求正常语句后必须跟分号（没记错的话这部分修改的文件是 [MyParser.java](./SimplifiedDatabase/cw-db-3_5/src/main/java/edu/uob/analyser/MyParser.java)）  
2. 让 Claude 帮忙写了两个 JUnit 测试文件（昨天打了至少10个小时的代码，一直在 debug ，晚上学了一会儿 JUnit 就困睡着了）：一个 [主测 SQL Command](./SimplifiedDatabase/cw-db-3_5/src/test/java/edu/uob/MyDbTests.java) ，一个 [主测有无分号](./SimplifiedDatabase/cw-db-3_5/src/test/java/edu/uob/MySemicolonTests.java) 🥲  
3. 对 id 隐患的修改没能完成，只留了一个 [未成型的类 + 类的说明](./SimplifiedDatabase/cw-db-3_5/src/main/java/edu/uob/storage/IdManager.java)  

另外还有一个问题，没时间修改也怕改错，是作业的这个 BNF 合法的语句是不能出现双引号的，但是在我的程序中可以。

今天早上去 Lab 发现有不少同学都在极限突击作业，或者说不少同学的 AI 在极限突击作业  
虽然我也用了 AI ，但听到有人说他花了几个小时就让 AI 生成可以完全跑的程序代码还是很难绷  
刚去到 Lab 就被另一个中国女生拉住，也不认识，不知道怎么就看上我了，让我帮她看代码怎么样、像不像 AI ··· 其实有点儿打乱了我的计划，所以最后并没有把我自己的代码修改成想象中的样子再交上去😮‍💨  

浅看了一下别人的代码，感觉我在 Lexer 和 Parser 阶段做得不好，或者说耦合性过高？应该先把所有字符都读取到了再交给 Parser ，而不是一边解析当前字符语义，一边读取下一个字符进行分类🤔  

还有一件心惊胆战的事情，发生了两三次，同一台电脑、同一组文件、没有任何改动，只是重新运行了一次同样的命令，第一次失败第二次就成功了，随机问了一个同学猜测说是环境问题； Teams 上也有同学问类似问题，比如会偶然超时报错退出，教授的回复说有可能是 Lab 的电脑的问题（同时有过多人在使用？）  

总而言之，下一一定测试驱动开发，开始敲代码前不管再难也一定把思路理清😭  

> 除了完成 Java 作业，本来有另一件值得高兴的事情，就是我前天得知我的电脑终于修好了，今天中午去拿，结果 FixMyTek 的人说还要收我 50 镑的维修费？？？当时他先说目前不确定最终金额，说之后确定好会邮件联系我让我可以先离开。我一开始有一点点不高兴，然后越想越气，最后直接折回去找他对峙，说他们从来没有事先告知我还要多付一笔钱（我先前付过 50 镑的费用，他说这只是检查费？），而且我的替换零件也是我自己买的，换算成英镑还不到一镑😓  
> 最后他说他会和经理商量这件事 ——  
> 大概一个小时后，就刚刚，我收到他的邮件说：  
>  
> ```  
> I've just spoken to my manager, and due to the miscommunication here on our end, I'm allowed to waive this fee.  
> My apologies for the communication breakdown here.  
> Thank you for your patience  
> ```  
>  
> 哈哈英国佬不仅速度慢 —— 换个充电接口换两个星期，收费贵 —— 换个充电接口想收我 100 镑，标准也挺灵活 —— 虽然很高兴不用付钱了，但总咽不下口气  
