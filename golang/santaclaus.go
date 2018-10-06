package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"

	"github.com/pkg/profile"
)

var reindeer = 9
var elves = 100
var wg sync.WaitGroup

func sleepThread(counter int) {
	time.Sleep(time.Duration(rand.Intn(counter)) * time.Millisecond)
}

func main() {
	defer profile.Start().Stop()
	//Buffered to number of reindeers remaining
	reindeersRemaining := make(chan int, reindeer)
	initReindeer(reindeersRemaining)

	reindeersFinished := make(chan int)
	santaAvailable := make(chan int)
	threeElves := make(chan int)
	elvesWaiting := make(chan int, 3)

	//Wait for reindeer to finish
	go waitForReindeer(reindeersFinished)
	//Start santa helping elves
	go helpElves(santaAvailable, threeElves)

	wg.Add(1)
	//Start the elves at random times
	for i := 0; i < elves; i++ {
		go getHelp(santaAvailable, elvesWaiting, threeElves, i)
	}

	for i := 0; i < reindeer; i++ {
		go arriveReindeer(reindeersRemaining, reindeersFinished)
	}
	//Make sure we wait till all elves have finished or the last reindeer arrives
	wg.Wait()
}

func initReindeer(reindeersRemaining chan int) {
	//Initialize to 9 reindeers remaining
	for i := 0; i < 9; i++ {
		reindeersRemaining <- 1
	}
}

func arriveReindeer(reindeersRemaining, reindeersFinished chan int) {
	sleepThread(2500)
	<-reindeersRemaining
	fmt.Println("New reindeer arrived")
	if len(reindeersRemaining) == 0 {
		fmt.Println("Last reindeer arrived, notifying santa")
		reindeersFinished <- 1
	}
}

func waitForReindeer(reindeersFinished chan int) {
	//Unbufferred channel, blocks till reindeers finished
	<-reindeersFinished
	fmt.Println("Reindeers finished, hitching sled!")
	wg.Done()
	return
}

func helpElves(santaAvailable, threeElves chan int) {
	for {
		//Wait till three elves have arrived
		<-threeElves
		fmt.Println("Helping three elves")
		//Helping
		sleepThread(250)
		//Signal you are available again
		santaAvailable <- 1

	}
}

func getHelp(santaAvailable, elvesWaiting, threeElves chan int, id int) {
	sleepThread(250)
	//Go into the queue of elves waiting
	elvesWaiting <- 1
	//If you are third elf, notify santa
	if len(elvesWaiting) == 3 {
		for i := 0; i < 3; i++ {
			//Clear all elves waiting before signaling that three elves are groupe
			<-elvesWaiting
		}
		threeElves <- 1
	}
	<-santaAvailable
	//Get santas help
	sleepThread(250)
	//No longer waiting
}
