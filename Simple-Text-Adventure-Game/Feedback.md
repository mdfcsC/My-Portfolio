# STAG Assignment Feedback

*Simon Lock <simon.lock@bristol.ac.uk>*

Marks for the STAG assignment should now be available via blackboard.  
Written feedback on your submission can be found below...  

Failed Test Cases (each failed test reported twice):  
[REPORT] Run through of entire marking game (and get the gold at the end !)...  
[REPORT] Checking that unique actions with more than one matched trigger phrase are performed...  
[REPORT] Checking that location is a valid subject for an action - we should still have an atom because we weren't in the right location to consume it...  
[REPORT] Checking that interpreter can cope with changed word order...  
[REPORT] Checking that entities are consumed during an action by trying to chop down the tree...  
[REPORT] Checking that unique actions with more than one matched trigger phrase are performed...  
[REPORT] Checking that entities are consumed during an action by trying to chop down the tree...  
[REPORT] Run through of entire marking game (and get the gold at the end !)...  
[REPORT] Checking that location is a valid subject for an action - we should still have an atom because we weren't in the right location to consume it...  
[REPORT] Checking that interpreter can cope with changed word order...  

Code Quality Feedback for he24343:  
Location.popArtefact(), Location.popCharacter(), Location.popFurniture(), Location.popEntity(), InputParser.compileMatchPattern(), ActionParser.parseActionPropertyToList(), EntityType.values(), EntityType.$values(), Player.popInventory() and Normaliser.normalizeString() could perhaps have been better named - they don't conform to recommendations (e.g. they aren't recognised verb/subject pairs or event handlers), are sometimes a bit too long, are just single words and start with an uppercase letter. The GameAction class does not always have blank lines to clearly separate the methods, which might make your code difficult to read. The following methods have high density of IF/CASE statements: CustomExecutor.processConsume(), CustomExecutor.executeAction(), CustomExecutor.processProduce(), BuiltInExecutor.executeGoto(), GameServer.handleCommand() and InputParser.parseInput(). Perhaps these could be implemented more elegantly ? The "CustomExecutor" class has deep nesting, which could make it difficult to maintain in the long term. Be aware that there are some tightly coupled pairs of classes in your code (BuiltInExecutor<->edu/uob/entity/Player, BuiltInExecutor<->edu/uob/entity/Location, CustomExecutor<->edu/uob/GameState and CustomExecutor<->edu/uob/entity/Location). The structure of your code may benefit from refactoring to help improve encapsulation and separation. Your code is somewhat lacking in useful and descriptive comments. The following methods have particularly complex structures: CustomExecutor.executeAction(), CustomExecutor.processConsume(), CustomExecutor.processProduce(), GameServer.handleCommand() and InputParser.parseInput() - this might make them difficult for developers to comprehend and maintain. The following methods appear to access global variables from other classes: BuiltInExecutor.executeGoto(), CustomExecutor.processConsume() and CustomExecutor.processProduce(). Remember - with Object Oriented programming we should encapsulate data and state within objects ! The following classes contain some unusual indentation: EntityParser, GameServer and CustomExecutor. Significant amounts of code has been duplicated in the following classes: ActionParser. Avoid copy-and-paste replication: keep your code DRY ! A number of classes contain some very long lines of code: GameServer, CustomExecutor and InputParser - are such long lines really necessary ? Your code contains a number of very similar "copy-and-pasted" methods: CustomExecutor.processConsume()-CustomExecutor.processProduce(). Remember to try to keep your code DRY !  

Derived code analysis report for: he24343  

Total number of 'raw' lines of code: 1510  
(includes every single line from all submitted files)  

Number of unique lines of executable code: 541  
(excluding blank lines, comments, imports, class/method signatures, base template code etc.)  

Number of lines of 'derived' code: 53  
(excluding blank lines, comments, imports, class/method signatures, base template code etc.)  

Classes with a significant quantity of derived code:  
EntityParser  

Percentage of executable code that consists of derived lines: 10%  

## Marks

![Marks](Java%20assignments%20marks.png)
