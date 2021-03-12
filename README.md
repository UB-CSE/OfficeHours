# OfficeHours
A web-based system to manage office hours

### Known Issues
1. [Failed to compile with Scala 2.13](https://github.com/UB-CSE/OfficeHours/issues/174)

### Contribution Guide

#### 1. How to Build this Project
1. Set up Scala SDK
2. Mark `src/main/scala` as `Source Root` if need
3. Set up the local configurations
    - Rename `src/main/resources/.env.example` to `src/main/resources/.env`
      
      Revise anything you need to change (Ex: change DB configurations. By default, it will
      use `List` as DB Driver, that means you don't really need to care about the DB connection
      if you're not familiar with it, but data(Ex: Queue data), will lose once you restart the project).

4. Build it.