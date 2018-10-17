# Quiz shell
Quiz shell for a kid.
## Building & Start
### Prerequisites
  * Maven 3.2.5 or higher
  * Java 1.8 or higher
### Build      
```
mvn package
```
It will create `quizshell-1.0-SNAPSHOT.jar`. 
### Start
There are several ways to start: 
- On Windows: `bin\quizshell.bat` 
- On Linux `bin/quizshell.sh`
- Just using java
  ```
   java -jar quizshell-1.0-SNAPSHOT.jar
  ``` 
 
## Currently supported commands  

Commands are case insensitive.
 
| Command | Description |
| ------- | ----------- |
| `h` | Print this help. |
|`plus <n> <m>` | Plus command to generate `n` `+` tasks with maximum number `m` in tasks.|
|`+ <n> <m>` | Alias for `plus` command.|
|`minus <n> <m>` |  Minus command to generate `n` `-` tasks with maximum number `m` in tasks.|
|`- <n> <m>` | Alias for `minus` command.|
|`set` | Set properties.|
| `q` | Quit the program.|
                        