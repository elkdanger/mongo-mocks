# mongo-mocks

[![Build Status](https://travis-ci.org/elkdanger/mongo-mocks.svg?branch=master)](https://travis-ci.org/elkdanger/mongo-mocks)
[ ![Download](https://api.bintray.com/packages/elkdanger/maven/mongo-mocks/images/download.svg) ](https://bintray.com/elkdanger/maven/mongo-mocks/_latestVersion)

A mocking helper library for Mockito + PlayReactiveMongo. It helps you create basic mocks for:

* Find operations, using either a collection or a single object
* Insert operations
* Update operations

Setups are performed on `JSONCollection` objects that have been mocked, either through `mock[JSONCollection]` or by using the helper `MockCollection()`.

All of the setup methods below can also be used either on `mock[JSONCollection]` or `MockCollectionBuilder` (as returned by `MockCollection()`). `MockCollectionBuilder` can also be implicitly converted to `JSONCollection` to make passing it around a lot easier.

## Finds that return a single entity

> Note: The find* methods usually have a DSL sugar method, or a normal method name - both are equivalent

The collection can be set up to return a single item when `find` is used. This essentially mocks the `one[T]` method:

```scala 
val collection = MockCollection()
val thing = mock[TestObject]

collection ~> Some(thing)

// OR...

collection.setupFind(Some(thing))

// collection.find(Json.obj()).one[TestObject] will 
// return a Future[Option[TestObject]] containing thing

```

Most of the time you will provide a filter to narrow down the thing you want to return. This is done using the ? method, before providing the return value:

```scala
val collection = MockCollection()
val thing = mock[TestObject]

collection ? ("id" -> 1) ~> Some(thing)

// OR...

collection.setupFind(Json.obj("id" -> 1), Some(thing))

// collection.find(Json.obj("id" -> 1)).one[TestObject] will 
// return a Future[Option[TestObject]] containing thing
```

## Finds that return collections

The same syntax can be used to set up finds that return collections of entities:

```scala 
val collection = MockCollection()
val listOfThings = List(mock[TestObject], mock[TestObject])

collection ~> listOfThings

OR...

collection.setupFind(listOfThings)

// collection.find(Json.obj()).cursor[TestObject]().collect[List]() will 
// return a Future[List(TestObject)] containing thing

```

This essentially mocks the query builder, cursor and collect methods on a Mongo collection. Filters can also be used in the same way to narrow down the results:

```scala 
val collection = MockCollection()
val listOfThings = List(mock[TestObject], mock[TestObject])

collection ? ("country" -> "UK") ~> listOfThings

OR...

collection.setupFind(Json.obj("country" -> "UK"), listOfThings)

// collection.find(Json.obj()).cursor[TestObject]().collect[List]() will 
// return a Future[List(TestObject)] containing thing

```

## Setting up inserts

When using insert, you will want to mock the operation so that it returns a true or false:

```scala
val collection = MockCollection()

collection.setupAnyInsert(fails = true)   // fails can be true or false
```

Or to narrow it down to inserting a specific document, you can use the `<~` method:

```scala
val collection = MockCollection()
val obj = mock[TestObject]

collection <~ obj

OR...

collection.setupInsertWith(obj)   // equivalent
```

## Verifying inserts

To verify that an insert was made with a particular object:

```scala
collection verifyInsertWith obj     // with either pass or fail assertion
```

To verify that an insert was made with any object:

```scala
collection.verifyAnyInsert
```

To verify that an insert was made, capturing the result for further verification:

```scala
val captor = ArgumentCaptor.forClass(classOf[TestObject])

collection verifyInsertWith captor

// captor.getValue will contain the object that was inserted
```

## Setting up updates

When using update operations, you'll want to return a value to indicate whether an update was successful:

```scala
collection.setupAnyUpdate(fails = true)   // can specify true or false to indicate success or failure
```

To set up using a selector and an object:

```scala
val selector = Json.obj("id" -> 2)
val obj = Json.obj("name" -> "John Doe")
val collection = MockCollection()

collection.setupUpdate(selector, obj)
```

## Verifying updates

To verify that any update was performed:

```scala
collection.verifyAnyUpdate
```

To verify that an update was made, optionally checking either the selector, the object, or both:

```scala
collection.verifyUpdate(
    selectorFunc = { _ should be Json.obj("id" -> 1) })
    
collection.verifyUpdate(
    objectFunc = { _ should be Json.obj("name" -> "John Doe") })
    
// or a combination of the two:
collection.verifyUpdate(
    selectorFunc = { _ should be Json.obj("id" -> 1) },
    objectFunc = { _ should be Json.obj("name" -> "John Doe") })
```
