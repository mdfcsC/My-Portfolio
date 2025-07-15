# My-Portfolio

Some minor achievements ...

[Minesweeper](#minesweeper)  
[DB](#simplified-database)  
[STAG](#simple-text-adventure-game)  
[Birthday Gift for a friend](#birthday-gift-for-a-friend)  
[A New Personal Website Main Page](#build-the-personal-website-pages-with-nextjs-and-vercel)  

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

Java 课的第一个大作业，三周时间写一个数据库 —— 我错了，在最后一周才开始造火箭，还是边学边造 😭  

有一些 [奇特的BNF语法规则](./SimplifiedDatabase/cw-db-3_4/introduction/BNF.txt) ，说是为了简化我们的代码实现，但是多的时间用来理解它的语法树了 —— 上学期的 Architecture 教得还是简单了 😓  

一些教师的授课资源： [任务布置](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/07-DB-Briefing.pdf) ， [方法指引](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/08-DB-Implementation.pdf) ， [工作手册](https://github.com/drslock/JAVA2024/tree/main/Weekly%20Workbooks/07%20DB%20assignment)  

2025年3月13日晚，经过将近一周的奋战，终于成功通过所有 BNF 需要的 SQL 命令的手动测试！反反复复写了好几版，目前这是 [第4版](./SimplifiedDatabase/cw-db-3_4/) ，决定用 GitHub 记录一下！  
花了两天时间，在 Claude、ChatGPT、DeepSeek 的帮助下，才大致想明白整个项目的框架；（这时间上学期的C的作业都快写完了）  
希望AI查重能通过，虽然我逢人就说“当然用 AI 写”，以及我真的遇到问题就会问 AI ，但是我真的手动写了很多“垃圾”，以至于今天下午约到 Tutor 让他帮我检查问题，他直呼我的代码逻辑太混乱了（幸好一个多小时终于解决了！ [主要的问题](./SimplifiedDatabase/cw-db-3_4/introduction/0313问题记录.txt) 是： `MySimpleCondition` 类中，如果本身是浮点数，是不能被 `Integer.parseInt()` 转为整形的，会直接导致错误抛出；以及一个 `Tokeniser` 中读取到数字时的循环逻辑问题。）另外，其实我对每列值的搜索会这么麻烦最后导致错误的主要原因：由于这个作业里面不要求对插入值进行类型检查，所以我只能根据用户输入的值的类型来把搜索列的值进行转换后比较，而 Tutor 说一般都会限制同一列的值的类型  

上课时教授说要自己编写 JUnit 测试，好好好正在开始学习什么是 JUnit ···  

除了还需要编写 JUnit 测试外，目前其实还有一个潜在的问题：根据教授的要求，每张表的每行记录的id自动生成并且不可更改、复用，我目前的逻辑时读取一张新表时扫描其最大的 `id` 值，之后的新插入数据在此基础上递增，但有一个边缘隐患——如果删除了最大 `id` 值的记录，储存了文件，然后再读取，那么下一条插入的记录的 `id` 会重复使用这个最大值。  

有一个 [run-mvnw.bash](./SimplifiedDatabase/cw-db-3_4/run-mvnw.bash) 文件，是 Claude 教我的方便在学校 MVB 的 Linux 电脑上用 Java17 运行程序的脚本。我之前在 Lab 的电脑上设置过一次 `JAVA_HOME`，但是这次去测代码发现又手动从 Java8 改到更新的版本···  

*2025-03-14*  

今天中午提交了最终作业。  

1. [最终版](./SimplifiedDatabase/cw-db-3_5/) 修改了对 SQL query 语句的要求，之前到代码运行结尾是 EOF 或者分号，现在要求正常语句后必须跟分号（没记错的话这部分修改的文件是 [MyParser.java](./SimplifiedDatabase/cw-db-3_5/src/main/java/edu/uob/analyser/MyParser.java) ）  
2. 让 Claude 帮忙写了两个 JUnit 测试文件（昨天打了至少10个小时的代码，一直在 debug ，晚上学了一会儿 JUnit 就困睡着了）：一个 [主测 SQL Command](./SimplifiedDatabase/cw-db-3_5/src/test/java/edu/uob/MyDbTests.java) ，一个 [主测有无分号](./SimplifiedDatabase/cw-db-3_5/src/test/java/edu/uob/MySemicolonTests.java) 🥲  
3. 对 id 隐患的修改没能完成，只留了一个 [未成型的类 + 类的说明](./SimplifiedDatabase/cw-db-3_5/src/main/java/edu/uob/storage/IdManager.java)  

另外还有一个问题，没时间修改也怕改错，是作业的这个 BNF 合法的语句是不能出现双引号的，但是在我的程序中可以。

今天早上去 Lab 发现有不少同学都在极限突击作业，或者说不少同学的 AI 在极限突击作业  
虽然我也用了 AI ，但听到有人说他花了几个小时就让 AI 生成可以完全跑的程序代码还是很难绷  
刚去到 Lab 就被另一个中国女生拉住，也不认识，不知道怎么就看上我了，让我帮她看代码怎么样、像不像 AI ··· 其实有点儿打乱了我的计划，所以最后并没有把我自己的代码修改成想象中的样子再交上去 😮‍💨  

浅看了一下别人的代码，感觉我在 Lexer 和 Parser 阶段做得不好，或者说耦合性过高？应该先把所有字符都读取到了再交给 Parser ，而不是一边解析当前字符语义，一边读取下一个字符进行分类 🤔  

还有一件心惊胆战的事情，发生了两三次，同一台电脑、同一组文件、没有任何改动，只是重新运行了一次同样的命令，第一次失败第二次就成功了，随机问了一个同学猜测说是环境问题； Teams 上也有同学问类似问题，比如会偶然超时报错退出，教授的回复说有可能是 Lab 的电脑的问题（同时有过多人在使用？）  

总而言之，下一一定测试驱动开发，开始敲代码前不管再难也一定把思路理清 😭  

> 除了完成 Java 作业，本来有另一件值得高兴的事情，就是我前天得知我的电脑终于修好了，今天中午去拿，结果 FixMyTek 的人说还要收我 50 镑的维修费？？？当时他先说目前不确定最终金额，说之后确定好会邮件联系我让我可以先离开。我一开始有一点点不高兴，然后越想越气，最后直接折回去找他对峙，说他们从来没有事先告知我还要多付一笔钱（我先前付过 50 镑的费用，他说这只是检查费？），而且我的替换零件也是我自己买的，换算成英镑还不到一镑 😓  
> 最后他说他会和经理商量这件事 ——  
> 大概一个小时后，就刚刚，我收到他的邮件说：  
>
> ```txt
> I've just spoken to my manager, and due to the miscommunication here on our end, I'm allowed to waive this fee.  
> My apologies for the communication breakdown here.  
> Thank you for your patience  
> ```
>
> 哈哈英国佬不仅速度慢 —— 换个充电接口换两个星期，收费贵 —— 换个充电接口想收我 100 镑，标准也挺灵活 —— 虽然很高兴不用付钱了，但总咽不下口气  

*2025-03-24*  

今天出了 [成绩和反馈](./SimplifiedDatabase/Feedback.md) 。  

早上看到班群里有人在问邮件里最后的百分比是成绩吗，赶忙打开邮件看 Simon 的反馈。一开始看见几个大字 “Failed” 觉得天塌了，这门课不会要挂了吧，拉到最后看到 15% 的百分比更是觉得彻底凉凉。  

再翻群里的聊天记录他们说成绩在 Blackboard 上，赶紧登录查看成绩 —— 如果是和上学期的 C 一样的打分标准，那也不算特别好，但也不算差。  

仔细看邮件的反馈，觉得应该问题挺多的，总体而言应该成绩不算好。有些问题（比如 DRY ）我之前也意识到了，但没时间改了，这次做 STAG 作业的时候一定留足时间、在编写时就注重 Code Quality ！  

## Simple Text Adventure Game

*2025-03-23*  

今天正式开始做本周一布置的 JAVA 课的大作业： [工作手册](https://github.com/drslock/JAVA2024/tree/main/Weekly%20Workbooks/10%20STAG%20assignment) 和 [课堂布置说明](https://github.com/drslock/JAVA2024/blob/main/Weekly%20Briefings/10-STAG-Briefing.pdf)  

在网上自己找到了游戏原型鼻祖 [Zork](https://tinyurl.com/zork-game) 的 [简介](https://zork.fandom.com/wiki/White_house) 和 [玩法](https://zork.fandom.com/wiki/Command_List)  

*2025-03-31*  

差不多基本完成，算是可以提交的一个状态了。  
一天奋斗一天玩天国拯救2，断断续续写了一周。写的时候没有上次那么痛苦，可能是时间充裕还可以边玩边写所以比较放松。今天尝试拆分模块稍微有点儿痛苦，因为本来已经写好了又要改，一边想怎么解耦合一边又怕改出 bug 😭  
虽然和 Tutor 在开始作业前以及作业中途分别约了一次答疑，但说实话感觉用处一般，没有带来很多的思路突破，但算是给了定心丸。

作业强行规定了 Illegal Constructs 检查， Simon 给了一个 Strange 工具去检查，但是得一个文件一个文件运行，所以让 Claude 帮忙写了一个 [自动化脚本](./Simple-Text-Adventure-Game/cw-stag/autoStrange.sh)  

话说这两天马上有朋友要过生日了不知道送什么礼物，本来想着写个小游戏或者拿这个文字游戏改一改送给她玩，但是不知道怎么打包，也没什么心思去创造完善游戏细节 😮‍💨  
说实话现在已经感觉很累不想打字了，还有想说的也懒得写了···  

*2025-04-02*  

天塌了... 今天发现虽然 `look` 的时候物品确实在地点、 `inventory` 之间传递，但是用 `entityParser` 中的 `locationHashMap()` 去获取 `Location` 然后查看该地点的 `entity` 时，没有物品的传递变化...  
不知道是什么原因，问了 Claude 和 Cursor 一晚上了也没什么进展，准备明天问下 Tutor 😭

回公寓吃了点速食盒饭，然后再问了一下 Claude ，居然解决了，原因是我写测试的时候调用的 `entityParser` `actionParser` `gameState` 和 `server` 用的不是同一个... 好低级的错误...  

现在的新问题是：  

1. 如果没有 `coin` 则 `fight elf and pay elf` 应当被识别成 `fight elf`  并且可以成功执行  
2. 如果输入的动作有歧义的问题（比如有两个门可以被打开）  

正在考虑把 `InputParser` 的逻辑改为先搜寻 `entity` ，判断有无超过一个动作的 `subject` ，没有的话再去判断 `trigger` 。  
说实话 Simon 的 README.md 写得也是很模棱两可，在 Teams 上的回复也是模棱两可，一天到晚讲的课也是模棱两可 + 不知所云的冷笑话 🙄  

*2025-04-03*  

昨天的第一个新问题解决了，按照昨天的那个想法改的。

第二个问题，我看不懂、不知道该怎么改、不想改了...  
第二个问题的相关要求如下：  

```markdown
**Ambiguous Commands**  
Much of the above "fuzzy" matching of actions is risky - there may be situations where _more than one_ action matches a particular command.
If a particular command is ambiguous (i.e. there is _more than one_ **valid** and **performable** action possible)
then NO action should be performed and a suitable warning message sent back to the user
(e.g. `there is more than one 'open' action possible - which one do you want to perform ?`)
```

*2025-04-25*  

昨天出了 [成绩](/Simple-Text-Adventure-Game/Feedback.md)  

### Some interesting quesitions on Teams

#### Possible Multiple Match

```txt
**Mikas Vong**
Hi Simon Lock, I'm wondering if we have the following actions in action file:
<action>
  <triggers>
    <keyphrase>acquire</keyphrase>
  </triggers>
  <subjects>
    <entity>coin</entity>
    <entity>axe</entity>
  </subjects>
        ...
  <narration>You acquire coin and used axe</narration>
</action>
 
<action>
  <triggers>
    <keyphrase>acquire</keyphrase>
  </triggers>
  <subjects>
    <entity>coin</entity>
  </subjects>
        ...
  <narration>You acquire coin</narration>
</action>
 
If the command is : "acquire coin with axe", should we:
carry out the first action, or 
since the second action's subject is satisfied-> ambiguous & presense of extraneous entitiy -> reject
```

```txt
**Simon Lock**
that's an interesting one - it would seem logical to perform the first action (since this is what the user seems to intend). However, this is going beyond what is asked for in the assignment brief (although I might add it as a test for next year ;o)
```

For heaven’s sake, shouldn’t the customized action adhere to the single-function principle??  

#### Where is the produced entity after player died?

```txt
**Xiaobo Ma**
if an action results in player death while producing entities, where should the produced entities be placed at? 
        <consumed>
            <entity>health</entity>
            <entity>health</entity>
            <entity>health</entity>
            <entity>TV</entity>
            <entity>water</entity>
        </consumed>
        <produced>
            <entity>frienda</entity>
            <entity>friendb</entity>
        </produced>
the player dies immediately, but where should frienda and friendb be? at the start location with the reborn player, or left at the player's last location (location where he dies)?
```

```txt
**Simon Lock**
it's not defined in the assignment brief - whatever you like
```

It’s worth improving my code in these parts!  

#### Behaviour of produced entities

```txt
**Shrirang Lokhande**
In the readme.md following description of Game Action is mentioned in Task 7.
 
When an entity is produced, it should be moved from its current location in the game (which might be in the storeroom) to the location in which the action was trigged. The entity should NOT automatically appear in a players inventory - it might be furniture (which the player can't carry) or it might be an artefact they don't actually want to pick up !
Can produced entities can be present  anywhere in the game (player's current location, outside of player's current location or storeroom), except other player's inventory?
```

```txt
**Simon Lock**
yes - that is correct
```

Why Simon said "correct"? Isn't that a contradiction in terms?  

## Birthday Gift for a friend  

*2025-04-01*  

Copy from [faahim's happy-birthday](https://github.com/faahim/happy-birthday#)  

朋友明天过生日，想送一个代码相关的礼物。但一是时间比较紧张，前段时间忙作业去了，二是自己技术力不够、想法不多。之前让 ChatGPT 和 DeepSeek 生成过几个网页小游戏，但效果一般。最后在 GitHub 上搜了一下别人的成果，感觉这个大神的很不错，审美在线，修改方便。感谢开源！  

在 Claude 和 Cursor 的帮助下，花了几个小时完成了 [这个礼物](https://mdfcsc.github.io/My-Portfolio/BirthdayGift-Jing/index.html) ！增加了音乐、点按快进等功能，把网络资源下载替换为本地资源（因为觉得国内会限制访问外网的一些东西），还纠结了一会儿要不要买国内的托管平台，因为网上很多说 GitHub Pages 在国内会限速或者打不开，但让朋友试了一下打开之前做的扫雷的游戏页面，她可以正常打开。接下来就等她反馈了哈哈哈哈哈哈哈 😆  

## Build the Personal Website Pages with Next.js And Vercel

*2025-07-15*

由于在学习 React 和 Next.JS ，于是想着更新下个人主页面来练练手。

> 为什么新建了一个仓库并使用 Vercel 部署？见 [Next-Portfolio 的记录](https://github.com/mdfcsC/Next-Portfolio?tab=readme-ov-file#personal-website-pages)

在B站上找了跟着 [学习 React 的视频](https://www.bilibili.com/video/BV1kH4y117yr/) 和 [学习 Next.JS 的视频](https://www.bilibili.com/video/BV157pRe8EyD/)  
可能本身学习和练习的就少，我一直觉得前端网页开发很烦、很难、很累，JS 的语法很神经、莫名其妙的样式就改了、 CSS 的名称我永远记不住、缓存异步回调一堆问题（现在还有一个静态网页部署的问题...）  
而且我觉得前端开发也挺考审美的

没有 Claude 老师的手把手帮助根本搭不出来这个 [新页面](https://mdfcsc-next-portfolio.vercel.app/)，顺便把 [这个仓库的主页面](https://mdfcsc.github.io/My-Portfolio/index.html) 设置了自动跳转
