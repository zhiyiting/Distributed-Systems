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

config_t *read_config();
point_t* init_dataset(config_t *);
point_t *init_centroid(point_t *, int, int);
void kmeans(point_t *, point_t *, int, int);
void write_result(point_t *, int, config_t *);
void print_result(point_t *, int);
int compare_replace(point_t *, point_t *, int);
cluster_sum_t *compute_cluster_sum(node_t **, int);
node_t **compute_cluster(point_t *, point_t *, int, int);

#endif