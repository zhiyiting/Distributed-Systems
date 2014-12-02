#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "kmeans.h"

#define THRESHOLD 0.01

config_t *read_config() {
	FILE *fp = fopen("config.txt", "r");
	if (fp == NULL) {
		fprintf(stderr, "Can't open config.txt!\n");
		exit(1);
	}
	int k;
	char input_filename[256];
	char output_filename[256];
	char temp[256];
	if (fscanf(fp, "%s %d", temp, &k) != 2) {
		fprintf(stderr, "Can't read cluster number k!\n");
		exit(1);
	}
	if (fscanf(fp, "%s %s", temp, input_filename) != 2) {
		fprintf(stderr, "Can't read input file name!\n");
		exit(1);
	}
	if (fscanf(fp, "%s %s", temp, output_filename) != 2) {
		fprintf(stderr, "Can't read output file name!\n");
		exit(1);
	}
	fclose(fp);
	config_t *config = (config_t *)malloc(sizeof(config_t));
	config->k = k;
	config->input_filename = malloc(strlen(input_filename));
	strcpy(config->input_filename, input_filename);
	config->output_filename = malloc(strlen(output_filename));
	strcpy(config->output_filename, output_filename);
	return config;
}

node_t* init_dataset(config_t *config) {
	FILE *fp = fopen(config->input_filename, "r");
	if (fp == NULL) {
		fprintf(stderr, "Can't open input file!\n");
		exit(1);
	}
	double d1, d2;
	int count = 0;
	node_t *dataset = NULL;
	while (!feof(fp)) {
		if (fscanf(fp, "%lf,%lf", &d1, &d2) != 2) break;
		node_t *p = (node_t *)malloc(sizeof(node_t));
		p->point.x = d1;
		p->point.y = d2;
		p->next = dataset;
		dataset = p;
		count++;
	}
	config->count = count;
	return dataset;
}

point_t *init_cluster(node_t *dataset, int k) {
	point_t *centroids = (point_t *)malloc(k * sizeof(point_t));
	node_t *head = dataset;
	for (int i = 0; i < k; i++) {
		centroids[i] = head->point;
		head = head->next;
	}
	return centroids;
}

double compute_square(double v) {
	return v * v;
}

double compute_distance(point_t p1, point_t p2) {
	return compute_square(p1.x - p2.x) + compute_square(p1.y - p2.y);
}

node_t *insert_point(node_t *ptr, point_t point) {
	node_t *node = (node_t *)malloc(sizeof(node_t));
	node->point = point;
	node->next = ptr;
	ptr = node;
	return ptr;
}

node_t **compute_cluster(node_t *data_ptr, point_t *centroids, int k) {
	node_t **cluster = (node_t **)malloc(k * sizeof(node_t *));
	while (data_ptr != NULL && k > 0) {
		point_t curpoint = data_ptr->point;
		double min_distance = compute_distance(curpoint, centroids[0]);;
		int index = 0;
		for (int i = 1; i < k; i++) {
			double distance = compute_distance(curpoint, centroids[i]);
			if (distance < min_distance) {
				min_distance = distance;
				index = i;
			}
		}
		cluster[index] = insert_point(cluster[index], curpoint);
		data_ptr = data_ptr->next;
	}
	return cluster;
}

point_t *compute_centroid(point_t *centroids, node_t **cluster, int k) {
	point_t *new_centroids = (point_t *)malloc(k * sizeof(point_t));
	for (int i = 0; i < k; i++) {
		node_t *p = cluster[i];
		int count = 0;
		double sum_x = 0;
		double sum_y = 0;
		point_t centroid;
		while (p != NULL) {
			sum_x += p->point.x;
			sum_y += p->point.y;
			count++;
			p = p->next;
		}
		centroid.x = sum_x / count;
		centroid.y = sum_y / count;
		new_centroids[i] = centroid;
	}
	return new_centroids;
}

int compare_replace(point_t *centroids, point_t *new_centroids, int k) {
	int flag = 1;
	for (int i = 0; i < k; i++) {
		if (compute_distance(centroids[i], new_centroids[i]) > THRESHOLD) {
			flag = 0;
		}
		centroids[i] = new_centroids[i];
	}
	return flag;
}

void write_result(point_t *centroids, int k, config_t *config) {
	FILE *fp = fopen(config->output_filename, "w");
	if (fp == NULL) {
		fprintf(stderr, "Can't open output file!\n");
		exit(1);
	}
	for (int i = 0; i < k; i++) {
		fprintf(fp, "%lf, %lf\n", centroids[i].x, centroids[i].y);
	}
	fclose(fp);
}

void print_result(point_t *centroids, int k) {
	for (int i = 0; i < k; i++) {
		printf("Cluster %d: %lf, %lf\n", i, centroids[i].x, centroids[i].y);
	}
	printf("-----------END OF PHASE-----------\n");
}

void kmeans(point_t *centroids, node_t *dataset, int k) {
	node_t **cluster = compute_cluster(dataset, centroids, k);
	point_t *new_centroids = compute_centroid(centroids, cluster, k);
	print_result(new_centroids, k);
	while (!compare_replace(centroids, new_centroids, k)) {
		cluster = compute_cluster(dataset, centroids, k);
		new_centroids = compute_centroid(centroids, cluster, k);
		print_result(new_centroids, k);
	}
}

int main() {
	config_t *config = read_config();
	int k = config->k;
	node_t *dataset = init_dataset(config);
	printf("dataset initiated\n");
	point_t *centroids = init_cluster(dataset, k);
	printf("Initial centroids picked\n");
	kmeans(centroids, dataset, k);
	print_result(centroids, k);
	write_result(centroids, k, config);
	return 0;
}