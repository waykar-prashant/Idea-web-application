package hello;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class InternetBrands {

	
	public static void main(String[] args) {
		//int arr[] = {1,2,3,4,5};
		//calculate(arr);
		
		int arr[] = {12, 1, 78, 90, 57, 89, 56};
		//int n = sizeof(arr)/sizeof(arr[0]);
		int k = 3;
		printKMax(arr, 7, 2);
		
	}
	
	static void printKMax(int arr[], int n, int k)
	{
		// Create a Double Ended Queue, Qi that will store indexes of array elements
		// The queue will store indexes of useful elements in every window and it will
		// maintain decreasing order of values from front to rear in Qi, i.e., 
		// arr[Qi.front[]] to arr[Qi.rear()] are sorted in decreasing order
		
		Deque<Integer> Qi = new ArrayDeque<>();

		/* Process first k (or first window) elements of array */
		int i;
		for (i = 0; i < k; ++i)
		{
			// For very element, the previous smaller elements are useless so
			// remove them from Qi
			while ( (!Qi.isEmpty()) && arr[i] <= arr[Qi.getLast()])
				Qi.removeLast(); // Remove from rear

			// Add new element at rear of queue
			Qi.addFirst(i);//
		}

		// Process rest of the elements, i.e., from arr[k] to arr[n-1]
		for ( ; i < n; ++i)
		{
			// The element at the front of the queue is the largest element of
			// previous window, so print it
			//cout << arr[Qi.front()] << " ";
			System.out.println(Qi.getFirst());
			// Remove the elements which are out of this window
			while ( (!Qi.isEmpty()) && Qi.getLast() >= i - k)
				Qi.removeLast();

			// Remove all elements smaller than the currently
			// being added element (remove useless elements)
			while ( (!Qi.isEmpty()) && arr[i] <= arr[Qi.getLast()])
				Qi.removeLast();

			// Add current element at the rear of Qi
			Qi.addLast(i);
		}

		// Print the maximum element of last window
		//cout << arr[Qi.front()];
		System.out.println(Qi.getFirst());
	}

	
	
	
	
	static void calculate(int arr[]){
		for(int i = 0; i < arr.length; i++){
			int item = calculateTotalChocolates(arr[i]);
			calculateTotalOdd(arr[i]);
			//System.out.println(item);
		}
	}

	private static int calculateTotalChocolates(int n) {
		//calculating the total odd numbers till n
		int total = 0;
		if(n > 0){
			if(n == 1)
				return 1;
			else
				return n * (n-1);
		}
		else
			return 0;
	}
	
	private static void calculateTotalOdd(int n){
		int total = 0;
		if(n%2==0){
			n = n-1;
		}
		System.out.println(((n+1)*(n+1))/4);
        /*for (int i = 0; i < n; i++) {
            if (n == 1) {
                System.out.println(1);
            } else {
            	//System.out.println((i * 2) + 1);
                total += (i * 2) + 1;
            }
        }*/
        //System.out.println("total : " + total);
	}
	
	

}
