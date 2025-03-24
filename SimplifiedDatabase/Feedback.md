## Final Score

69/100

## Failed Test Cases (each reported twice)

- [REPORT] Testing that server permits > comparator for strings...
- [REPORT] Testing that query returns an empty table for incompatible types comparison...
- [REPORT] Testing that numerical LIKE queries are performed using string matching...
- [REPORT] Testing that server does not recycle IDs event after deletion of rows and server restart...
- [REPORT] Testing the JOIN query on full populated movie and roles tables...
- [REPORT] Testing that a missing comma is detected...
- [REPORT] Testing that a table can't contain duplicate column names...
- [REPORT] Testing the JOIN query on full populated movie and roles tables...
- [REPORT] Testing that a table can't contain duplicate column names...
- [REPORT] Testing that server does not recycle IDs event after deletion of rows and server restart...
- [REPORT] Testing that server permits > comparator for strings...
- [REPORT] Testing that query returns an empty table for incompatible types comparison...
- [REPORT] Testing that a missing comma is detected...
- [REPORT] Testing that numerical LIKE queries are performed using string matching...

## Code Quality Feedback

Your method names are often relatively short (for example `.`). As a result, it might prove difficult to infer the purpose of these methods. A significant number of your method names are potentially problematic—some are just single words, don't conform to recommendations (e.g., they aren't recognised verb/subject pairs or event handlers) and start with an uppercase letter.

The following methods are really quite long:

- `JoinCommand.execute()`
- `FileManager.loadTableFile()`
- `MyParser.parseCondition()`
- `MyParser.parseUpdateCommand()`

Could some of these be refactored into shorter, separate methods?

Your variable names are frequently very short (for example `type`, `value`, `input`, `QUOTE`, `EOF`, `OTHER`). As a result, it might prove difficult to infer the purpose of these variables.

The following methods have a high density of IF/CASE statements:

- `Tokeniser.nextToken()`
- `MyParser.parseDeleteCommand()`
- `MyParser.parseDropCommand()`
- `MyParser.parseInsertCommand()`
- `MyParser.parseSelectCommand()`
- `FileManager.deleteDirOrFile()`
- `FileManager.loadTableFile()`
- `MyParser.parseCondition()`
- `MyParser.parseInput()`
- `MyParser.parseUpdateCommand()`
- `MySimpleCondition.evaluate()`
- `MyParser.parseJoinCommand()`
- `MyParser.parseCrtTableCmd()`

Perhaps these could be implemented more elegantly?

The following classes have deep nesting:

- `FileManager`
- `MyParser`

This could make them difficult to maintain in the long term.

Be aware that there are some tightly coupled pairs of classes in your code:

- `JoinCommand` <-> `edu/uob/model/Row`
- `MyParser` <-> `edu/uob/analyser/MyToken`

The structure of your code may benefit from refactoring to help improve encapsulation and separation.

Your code is somewhat lacking in useful and descriptive comments.

The following methods have particularly complex structures:

- `FileManager.deleteDirOrFile()`
- `FileManager.loadTableFile()`
- `JoinCommand.execute()`
- `MyParser.parseInput()`
- `MyParser.parseCrtTableCmd()`
- `MyParser.parseDropCommand()`
- `MyParser.parseAlterCommand()`
- `MyParser.parseInsertCommand()`
- `MyParser.parseSelectCommand()`
- `MyParser.parseCondition()`
- `MyParser.parseUpdateCommand()`
- `MyParser.parseDeleteCommand()`
- `MyParser.parseJoinCommand()`
- `MySimpleCondition.evaluate()`
- `Tokeniser.nextToken()`
- `Tokeniser.readSomething()`
- `Tokeniser.readOperator()`

This might make them difficult for developers to comprehend and maintain.

The following classes contain some unusual indentation:

- `Tokeniser`
- `MyParser`

Significant amounts of code have been duplicated in the following classes:

- `MyParser`
- `FileManager`
- `AltTbAddColumnCmd`
- `AltTbDropColumnCmd`
- `InsertCommand`
- `DeleteCommand`
- `UpdateCommand`
- `MySimpleCondition`

Avoid copy-and-paste replication: keep your code **DRY**!

A number of classes contain some very long lines of code:

- `FileManager`
- `Tokeniser`
- `MyParser`

Are such long lines really necessary?

You have a significant number of very similar "copy-and-pasted" methods—remember to keep your code **DRY**!

## Derived Code Analysis Report

- **Total number of 'raw' lines of code:** 2642  
  *(includes every single line from all submitted files)*

- **Number of unique lines of executable code:** 885  
  *(excluding blank lines, comments, imports, class/method signatures, base template code, etc.)*

- **Number of lines of 'derived' code:** 135  
  *(excluding blank lines, comments, imports, class/method signatures, base template code, etc.)*

### Classes with a significant quantity of derived code

- `FileManager`
- `JoinCommand`
- `MyParser`
- `TokenType`

- **Percentage of executable code that consists of derived lines:** 15%
