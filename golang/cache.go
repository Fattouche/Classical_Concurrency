package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

func sleepThread(counter int) {
	time.Sleep(time.Duration(rand.Intn(counter)) * time.Millisecond)
}

type object struct {
	expiration int64
	value      interface{}
}

type cache struct {
	objects map[string]object
	mutex   sync.RWMutex
}

func main() {
	myCache := createCache()
	wg := sync.WaitGroup{}
	wg.Add(3)
	go func() {
		fmt.Println("Inserting 1-10")
		myCache.insert("1", 1, 2*time.Second)
		myCache.insert("2", 2, 5*time.Second)
		myCache.insert("3", 3, 3*time.Second)
		myCache.insert("4", 4, 2*time.Second)
		myCache.insert("5", 5, 5*time.Second)
		myCache.insert("6", 6, 3*time.Second)
		myCache.insert("7", 7, 2*time.Second)
		myCache.insert("8", 8, 5*time.Second)
		myCache.insert("9", 9, 3*time.Second)
		myCache.insert("10", 10, 1*time.Second)
		wg.Done()
	}()
	go func() {
		sleepThread(100)
		fmt.Println("Searching 1")
		ok := false
		var temp interface{}
		if temp, ok = myCache.get("1"); !ok {
			fmt.Println("Failed to find 1")
		} else {
			fmt.Println("Found 1 with value ", temp.(object).value)
		}

		sleepThread(100)
		fmt.Println("Searching 2")
		if temp, ok = myCache.get("2"); !ok {
			fmt.Println("Failed to find 2")
		} else {
			fmt.Println("Found 2 with value ", temp.(object).value)
		}

		sleepThread(100)
		fmt.Println("Searching 5")
		if temp, ok = myCache.get("5"); !ok {
			fmt.Println("Failed to find 5")
		} else {
			fmt.Println("Found 5 with value ", temp.(object).value)
		}

		sleepThread(100)
		fmt.Println("Searching 6")
		if temp, ok = myCache.get("6"); !ok {
			fmt.Println("Failed to find 6")
		} else {
			fmt.Println("Found 6 with value ", temp.(object).value)
		}
		wg.Done()
	}()

	go func() {
		sleepThread(250)
		fmt.Println("Deleting 1")
		ok := false
		if ok = myCache.remove("1"); !ok {
			fmt.Println("Failed to delete 1")
		}
		fmt.Println("deleted 1 from cache")

		sleepThread(250)
		fmt.Println("Deleting 2")
		if ok = myCache.remove("2"); !ok {
			fmt.Println("Failed to delete 2")
		}
		fmt.Println("deleted 2 from cache")

		sleepThread(250)
		fmt.Println("Deleting 7")
		if ok = myCache.remove("7"); !ok {
			fmt.Println("Failed to delete 7")
		}
		fmt.Println("deleted 7 from cache")

		sleepThread(250)
		fmt.Println("Deleting 9")
		if ok = myCache.remove("9"); !ok {
			fmt.Println("Failed to delete 9")
		}
		fmt.Println("deleted 9 from cache")
		wg.Done()
	}()
	wg.Wait()
}

func createCache() *cache {
	var temp cache
	temp.mutex = sync.RWMutex{}
	temp.objects = make(map[string]object)
	return &temp
}

func isExpired(obj object) bool {
	if obj.expiration == 0 {
		return true
	}
	return obj.expiration < time.Now().Unix()
}

func (c *cache) insert(key string, val interface{}, duration time.Duration) bool {
	time := time.Now().Add(duration).Unix()
	c.mutex.Lock()
	defer c.mutex.Unlock()
	if _, ok := c.objects[key]; ok {
		return false
	}
	c.objects[key] = object{
		expiration: time,
		value:      val,
	}
	return true
}

func (c *cache) remove(key string) bool {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	obj, ok := c.objects[key]
	if isExpired(obj) {
		delete(c.objects, key)
		return false
	}
	delete(c.objects, key)
	return ok
}

func (c *cache) exists(key string) bool {
	c.mutex.RLock()
	defer c.mutex.RUnlock()
	obj, ok := c.objects[key]
	if isExpired(obj) {
		delete(c.objects, key)
		return false
	}
	return ok
}

func (c *cache) get(key string) (interface{}, bool) {
	c.mutex.RLock()
	defer c.mutex.RUnlock()
	obj, ok := c.objects[key]
	if isExpired(obj) {
		delete(c.objects, key)
		return nil, false
	}
	return obj, ok
}
