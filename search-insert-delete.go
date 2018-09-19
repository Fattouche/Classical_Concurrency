package main

import (
	"container/list"
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var mutexDelete sync.RWMutex
var mutexInsert sync.Mutex
var wg sync.WaitGroup
var myList list.List

var searchers = 50
var inserters = 20
var deleters = 10

func sleep() {
	time.Sleep(time.Duration(rand.Intn(100)) * time.Millisecond)
}

func main() {
	fmt.Println("Starting inserters")
	myList.PushBack(1)
	myList.PushBack(2)
	myList.PushBack(3)

	wg.Add(inserters)
	go func() {
		for i := 0; i < inserters; i++ {
			time.Sleep(time.Duration(rand.Intn(250)) * time.Millisecond)
			go insert()
		}
	}()
	fmt.Println("Start searchers")
	wg.Add(searchers)
	go func() {
		for i := 0; i < searchers; i++ {
			time.Sleep(time.Duration(rand.Intn(100)) * time.Millisecond)
			go search()
		}
	}()

	fmt.Println("Starting deleters")
	wg.Add(deleters)
	go func() {
		for i := 0; i < searchers; i++ {
			time.Sleep(time.Duration(rand.Intn(500)) * time.Millisecond)
			go delete()
		}
	}()
	wg.Wait()
}

func search() {
	sleep()
	//search only needs to readlock the delete mutex
	mutexDelete.RLock()
	fmt.Println("Searched: ", myList.Front().Value)
	sleep()
	mutexDelete.RUnlock()
	wg.Done()
}

func insert() {
	sleep()
	//Insert needs to lock the insert mutex to make sure no other inserts and also readlock the delete mutex
	mutexInsert.Lock()
	mutexDelete.RLock()
	val := rand.Intn(100)
	fmt.Printf("Inserting: %d\n", val)
	myList.PushFront(val)
	sleep()
	mutexInsert.Unlock()
	mutexDelete.RUnlock()
	wg.Done()
}

func delete() {
	sleep()
	//Need to lock both the insert and delete mutex so no one else is doing anything
	mutexInsert.Lock()
	mutexDelete.Lock()
	val := myList.Back()
	fmt.Printf("Deleting: %d\n", val.Value)
	myList.Remove(val)
	sleep()
	mutexInsert.Unlock()
	mutexDelete.Unlock()
	wg.Done()
}
