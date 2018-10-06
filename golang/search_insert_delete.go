package main

import (
	"container/list"
	"fmt"
	"math/rand"
	"sync"
	"time"

	"github.com/pkg/profile"
)

var mutexDelete sync.RWMutex
var mutexInsert sync.Mutex
var wg sync.WaitGroup
var myList list.List

var searchers = 50
var inserters = 20
var deleters = 10

func sleepThread(counter int) {
	time.Sleep(time.Duration(rand.Intn(counter)) * time.Millisecond)
}
func main() {
	defer profile.Start().Stop()
	fmt.Println("Starting inserters")
	myList.PushBack(1)
	myList.PushBack(2)
	myList.PushBack(3)

	wg.Add(inserters)
	go func() {
		for i := 0; i < inserters; i++ {
			sleepThread(250)
			go insert()
		}
	}()
	fmt.Println("Starting searchers")
	wg.Add(searchers)
	go func() {
		for i := 0; i < searchers; i++ {
			sleepThread(100)
			go search()
		}
	}()

	fmt.Println("Starting deleters")
	wg.Add(deleters)
	go func() {
		for i := 0; i < searchers; i++ {
			sleepThread(500)
			go delete()
		}
	}()
	wg.Wait()
}

func search() {
	sleepThread(250)
	//search only needs to readlock the delete mutex
	mutexDelete.RLock()
	fmt.Println("Searched: ", myList.Front().Value)
	sleepThread(250)
	mutexDelete.RUnlock()
	wg.Done()
}

func insert() {
	sleepThread(250)
	//Insert needs to lock the insert mutex to make sure no other inserts and also readlock the delete mutex
	mutexInsert.Lock()
	mutexDelete.RLock()
	val := rand.Intn(100)
	fmt.Printf("Inserting: %d\n", val)
	myList.PushFront(val)
	sleepThread(250)
	mutexInsert.Unlock()
	mutexDelete.RUnlock()
	wg.Done()
}

func delete() {
	sleepThread(250)
	//Need to lock both the insert and delete mutex so no one else is doing anything
	mutexInsert.Lock()
	mutexDelete.Lock()
	val := myList.Back()
	fmt.Printf("Deleting: %d\n", val.Value)
	myList.Remove(val)
	sleepThread(250)
	mutexInsert.Unlock()
	mutexDelete.Unlock()
	wg.Done()
}
