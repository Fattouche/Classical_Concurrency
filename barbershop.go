package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var chairs = 5
var customers = 100
var wg sync.WaitGroup

func main() {
	//Buffered to number of chairs
	chairs := make(chan int, chairs)
	//Blocks on both ends untill barber and customer are both ready
	barberAvailable := make(chan int)
	//Start the barber
	go cutHair(chairs, barberAvailable)

	//Start the customers at random times
	for i := 0; i < customers; i++ {
		wg.Add(1)
		time.Sleep(time.Duration(rand.Intn(250)) * time.Millisecond)
		go getHairCut(chairs, barberAvailable, i)
	}
	//Make sure we wait till all the customers have visited the shop
	wg.Wait()
}

func cutHair(chairs, barberAvailable chan int) {
	for {
		//Wait till someone enters
		barberAvailable <- 1
		//Free up a chair
		<-chairs
		//cut hair
		time.Sleep(time.Duration(rand.Intn(500)) * time.Millisecond)
	}
}

func getHairCut(chairs, barberAvailable chan int, id int) {
	defer wg.Done()
	fmt.Printf("Customer %d: Arriving to barbershop\n", id)
	//Check if barber is available
	select {
	case <-barberAvailable:
		fmt.Printf("Customer %d: Getting haircut\n", id)
		return
	default:

	}

	select {
	case chairs <- 1:
		//Sit in chair
		fmt.Printf("Customer %d: Waiting in chair\n", id)
		//Wait till barber is available
		<-barberAvailable
		fmt.Printf("Customer %d: Getting haircut\n", id)
		return
	default:
		fmt.Printf("Customer %d: All chairs taken, leaving\n", id)
	}
}
