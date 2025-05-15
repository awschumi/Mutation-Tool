#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * 1) Dynamically allocating a char array
 * 2) Copy the content of the input into the new one without checking the size
 * 3) Free the array
 */
void vulnerable_function(char *input) {
    char *heap_buffer = malloc(32);
    if (!heap_buffer) {
        perror("malloc failed");
        exit(1);
    }
    strcpy(heap_buffer, input); // No bounds checking
    printf("Copied input: %s", heap_buffer);
    free(heap_buffer);
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <input>", argv[0]);
        return 1;
    }
    vulnerable_function(argv[1]);
    return 0;
}