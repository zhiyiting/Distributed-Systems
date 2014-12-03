#ifndef _KMEANS_H_
#define _KMEANS_H_

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <time.h>

#define THRESHOLD 0.01

typedef struct {
	int k, count;
	char *input_filename;
	char *output_filename;
} config_t;

typedef struct {
	double x, y;
} point_t;

typedef struct node_t {
	point_t point;
	struct node_t *next;
} node_t;

typedef struct {
	double sum_x, sum_y;
	int count;
} cluster_sum_t;

#endif