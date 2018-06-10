
# Util

Util functions for Java/Kotlin projects.

[![Jitpack](https://jitpack.io/v/JulianoZanella/util.svg)](https://jitpack.io/#JulianoZanella/util)
[![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)



## Features

- Supports Kotlin or Java Classes.
- Mysql support
- insert, update, delete and select by dataClasses
- [Examples](https://github.com/JulianoZanella/util-samples)
 
 
### Add *Util* to your project

It's very simple add *Util* to your project with [![](https://jitpack.io/v/JulianoZanella/util.svg)](https://jitpack.io/#JulianoZanella/util)

#### Maven

##### *Step 1*. Add the JitPack repository to your build file

```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
```

##### *Step 2*. Add the dependency
```xml
<dependency>
    <groupId>com.github.JulianoZanella</groupId>
    <artifactId>util</artifactId>
    <version>v1.0.0</version>
</dependency>
```	

#### Gradle

##### Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
##### Add the dependency:

```gradle
dependencies {
        implementation 'com.github.JulianoZanella:util:v1.0.0'
}
```

### What does it look like? (Code snippets)

#### Connect MySQL database

**Important: This step must be performed before any interaction in the database, usually in the Main class**


Java:
```java
final String URL = "jdbc:mysql://localhost:3306/databaseName";
final String USER = "root";
final String PASSWORD = "";
Database.createConnection(URL, USER, PASSWORD);
```

Kotlin:
```kotlin
val URL = "jdbc:mysql://localhost:3306/databaseName"
val USER = "root"
val PASSWORD = ""
Database.createConnection(URL, USER, PASSWORD)
```
    
#### Insert data to database


Java:
```java
Person person = new Person(1, "Foo", birthDate, 'M');
Database.insert(person);
```

Kotlin:
```kotlin
val person = Person(1, "Foo", birthDate , 'O')
Database.insert(person)
```

#### Update data on database


Java:
```java
Person person = new Person(1, "Foo", birthDate, 'M');
Database.update(person);
```

Kotlin:
```kotlin
val person = Person(1, "Foo", birthDate , 'O')
Database.update(person)
```

#### Delete data on database


Java:
```java
Person person = new Person();
person.setId(1);
Database.delete(person);
```

Kotlin:
```kotlin
val person = Person()
person.id = 1
Database.delete(person)
```

#### Retrieve data from database


Java:
```java
List<Person> persons = new ArrayList<>();
List<Object> objectList = Database.select(Person.class);
for (Object object : objectList) {
  Person p = (Person) object;
  persons.add(p);
}
```

Kotlin:
```kotlin
val persons = ArrayList<Person>()
val objectList = Database.select(Person::class.java)
for (`object` in objectList) {
     val p = `object` as Person
     persons.add(p)
 }
```
