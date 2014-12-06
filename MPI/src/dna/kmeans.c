#include "kmeans.h"

/* compute similarity between two dna strands */
int compute_distance(dna_t p1, dna_t p2, int l) {
    int count = 0;
    char *s1 = p1.acid;
    char *s2 = p2.acid;
    /* calculate the count of same character from two strands */
    for (int i = 0; i < l; i++) {
        if (s1[i] == s2[i]) {
            count++;
        }
    }
    return count;
}

/* generate a random double between 0 and 1 */
double generate_random_double() {
    return (double)rand() / (double)RAND_MAX;
}

/* write the centroids result on file declared in config file */
void write_result(dna_t *centroids, int l, int k, config_t *config) {
    FILE *fp = fopen(config->output_filename, "w");
    if (fp == NULL) {
        fprintf(stderr, "Can't open output file!\n");
        exit(1);
    }
    /* iterate through the centroids and write the result */
    for (int i = 0; i < k; i++) {
        char *s = centroids[i].acid;
        fprintf(fp, "Cluster %d: %c", i, s[0]);
        for (int j = 1; j < l; j++) {
            fprintf(fp, ",%c", s[j]);
        }
        fprintf(fp, "\n");
    }
    fclose(fp);
}

/* print dna */
void print_dna(dna_t dna, int l) {
    char *s = dna.acid;
    for (int i = 0; i < l; i++) {
        printf("%c,", s[i]);
    }
    printf("\n");
}

/* print the centroids result */
void print_result(dna_t *centroids, int l, int k) {
    /* iterate through the centroids and print the result */
    for (int i = 0; i < k; i++) {
        char *s = centroids[i].acid;
        printf("Cluster %d: %c", i, s[0]);
        for (int j = 1; j < l; j++) {
            printf(",%c", s[j]);
        }
        printf("\n");
    }
}

/* read information from config file */
config_t *read_config() {
    FILE *fp = fopen("config.txt", "r");
    if (fp == NULL) {
        fprintf(stderr, "Can't open config.txt!\n");
        exit(1);
    }
    int k, l, count;
    char input_filename[256];
    char output_filename[256];
    char temp[256];
    /* read the cluster number */
    if (fscanf(fp, "%s %d", temp, &k) != 2) {
        fprintf(stderr, "Can't read cluster number!\n");
        exit(1);
    }
    /* read the point count per cluster */
    if (fscanf(fp, "%s %d", temp, &count) != 2) {
        fprintf(stderr, "Can't read point count!\n");
        exit(1);
    }
    /* read the dna length */
    if (fscanf(fp, "%s %d", temp, &l) != 2) {
        fprintf(stderr, "Can't read dna length!\n");
        exit(1);
    }
    /* read the input file name */
    if (fscanf(fp, "%s %s", temp, input_filename) != 2) {
        fprintf(stderr, "Can't read input file name!\n");
        exit(1);
    }
    /* read the output file name */
    if (fscanf(fp, "%s %s", temp, output_filename) != 2) {
        fprintf(stderr, "Can't read output file name!\n");
        exit(1);
    }
    fclose(fp);
    /* store the information in the config structure */
    config_t *config = (config_t *)malloc(sizeof(config_t));
    config->k = k;
    config->l = l;
    config->count = count * k;
    config->input_filename = (char *)malloc(strlen(input_filename)+1);
    strcpy(config->input_filename, input_filename);
    config->output_filename = (char *)malloc(strlen(output_filename)+1);
    strcpy(config->output_filename, output_filename);
    return config;
}

/* initialize dataset from config file */
dna_t* init_dataset(config_t *config) {
    FILE *fp = fopen(config->input_filename, "r");
    if (fp == NULL) {
        fprintf(stderr, "Can't open input file!\n");
        exit(1);
    }
    int count = config->count;
    int l = config->l;
    dna_t *dataset = (dna_t *)malloc(count * sizeof(dna_t));
    /* read information from file*/
    for (int i = 0; i < count; i++) {
        char *content = (char *)calloc(l, sizeof(char));
        char temp;
        fscanf(fp, "%c", &content[0]);
        for (int j = 1; j < l; j++) {
            fscanf(fp, ",%c", &content[j]);
        }
        fscanf(fp, "\n");
        dataset[i].acid = content;
    }
    return dataset;
}

/* Select k initial centroids according to kmeans++ algorithm */
/* Kmeans++ reference: http://dl.acm.org/citation.cfm?id=1283494 */
dna_t *init_centroid(dna_t *dataset, int l, int k, int n) {
    dna_t *centroids = (dna_t *)malloc(k * sizeof(dna_t));
    int *min_similarity = (int *)malloc(n * sizeof(int));
    int *visited = (int *)malloc(n * sizeof(int));
    time_t t;
    srand((unsigned) time(&t));
    // for simplicity, select the first point as a centroid
    centroids[0] = dataset[0];
    for (int i = 1; i < n; i++) {
        min_similarity[i] = compute_distance(centroids[0], dataset[i], l);
        visited[i] = 0;
    }
    visited[0] = 1;
    // initialize the dist sum of unvisited nodes
    for (int i = 1; i < k; i++) {
        int sum_dist = 0;
        for (int j = 0; j < n; j++) {
            if (!visited[j]) {
                sum_dist += min_similarity[j];
            }
        }
        // each point is chosen with probability porpotional to D(x)2
        double r = generate_random_double() * sum_dist;
        // the index to be added as a centroid
        int next_index = -1;
        int temp_sum = 0;
        // sum through the distance, stops when sum >= r
        for (int j = 0; j < n; j++) {
            if (!visited[j]) {
                temp_sum += min_similarity[j];
                if (temp_sum >= r) {
                    next_index = j;
                    break;
                }
            }
        }
        // if not found, use the last unvisited node as centroid
        if (next_index == -1) {
            for (int j = n - 1; j >= 0; j--) {
                if (!visited[j]) {
                    next_index = j;
                    break;
                }
            }
        }
        // if found, add it to the centroid array
        // recalculate the similarity array
        if (next_index >= 0) {
            visited[next_index] = 1;
            dna_t new_centroid = dataset[next_index];
            centroids[i] = new_centroid;
            if (i < k - 1) {
                for (int j = 0; j < n; j++) {
                    if (!visited[j]) {
                        int new_dist = compute_distance(dataset[j], new_centroid, l);
                        if (new_dist < min_similarity[j]) {
                            min_similarity[j] = new_dist;
                        }
                    }
                }
            }
        }
        else {
            printf("Error selecting centroids\n");
            exit(1);
        }
    }
    return centroids;
}

/* insert a point before a node and make it the head of a list */
node_t *insert_point(node_t *ptr, dna_t strand) {
    node_t *node = (node_t *)malloc(sizeof(node_t));
    node->strand = strand;
    node->next = ptr;
    ptr = node;
    return ptr;
}

/* compute k clusters from dataset and centroids */
node_t **compute_cluster(dna_t *dataset, dna_t *centroids, int l, int k, int n) {
    node_t **cluster = (node_t **)calloc(k, sizeof(node_t *));
    if (k <= 0) {
        printf("Invalid cluster number\n");
        exit(1);
    }
    for (int j = 0; j < n; j++) {
        // for each point, compute its closet centroid
        dna_t curpoint = dataset[j];
        int min_distance = compute_distance(curpoint, centroids[0], l);
        int index = 0;
        for (int i = 1; i < k; i++) {
            int distance = compute_distance(curpoint, centroids[i], l);
            if (distance < min_distance) {
                min_distance = distance;
                index = i;
            }
        }
        // add the point to that cluster
        cluster[index] = insert_point(cluster[index], curpoint);
    }
    return cluster;
}

/* compute [sum, count] pair for each cluster */
cluster_sum_t *compute_cluster_sum(node_t **cluster, int l, int k) {
    cluster_sum_t *cluster_sum = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));
    // initialize the cluster_sum array
    for (int i = 0; i < k; i++) {
        node_t *p = cluster[i];
        int **count = (int **)malloc(l * sizeof(int *));
        for (int j = 0; j < l; j++) {
            count[j] = (int *)malloc(4 * sizeof(int));
            for (int k = 0; k < 4; k++) {
                count[j][k] = 0;
            }
        }
        // for each index, compute the count of A, C, G, T respectively
        while (p != NULL) {
            char* acid = p->strand.acid;
            for (int j = 0; j < l; j++) {
                switch(acid[j]) {
                    case 'A':
                        count[j][0]++;
                        break;
                    case 'C':
                        count[j][1]++;
                        break;
                    case 'G':
                        count[j][2]++;
                        break;
                    case 'T':
                        count[j][3]++;
                        break;
                }
            }
            p = p->next;

        }
        cluster_sum[i].count = count;
    }
    return cluster_sum;
}

/* compute mean point as centroid for clusters */
dna_t *compute_centroid(node_t **cluster, int l, int k) {
    cluster_sum_t *cluster_sum = compute_cluster_sum(cluster, l, k);
    dna_t *new_centroids = (dna_t *)malloc(k * sizeof(dna_t));
    char base[4] = {'A', 'C', 'G', 'T'};
    // for each cluster, compute the centroid
    for (int i = 0; i < k; i++) {
        dna_t centroid;
        char *content = (char *)malloc(l * sizeof(char));
        cluster_sum_t ccluster = cluster_sum[i];
        // use the max count of alphabet as the mean
        for (int j = 0; j < l; j++) {
            int count = ccluster.count[j][0];
            int index = 0;
            for (int b = 1; b < 4; b++) {
                int temp = ccluster.count[j][b];
                if (count < temp) {
                    count = temp;
                    index = b;
                }
            }
            content[j] = base[index];
        }
        centroid.acid = content;
        new_centroids[i] = centroid;
    }
    free(cluster_sum);
    return new_centroids;
}

/* return two centroids if they are close enough */
/* replace the old with new centroids */
int compare_replace(dna_t *centroids, dna_t *new_centroids, int l, int k) {
    int flag = 1;
    for (int i = 0; i < k; i++) {
        if (compute_distance(centroids[i], new_centroids[i], l) > THRESHOLD) {
            flag = 0;
        }
        centroids[i] = new_centroids[i];
    }
    return flag;
}

/* a sequential kmeans process */
void kmeans(dna_t *centroids, dna_t *dataset, int l, int k, int n) {
    /* compute and initialize the cluster */
    node_t **cluster = compute_cluster(dataset, centroids, l, k, n);
    /* compute new centroids */
    dna_t *new_centroids = compute_centroid(cluster, l, k);
    /* if there's a big difference, recompute the centroid */
    while (!compare_replace(centroids, new_centroids, l, k)) {
        cluster = compute_cluster(dataset, centroids, l, k, n);
        new_centroids = compute_centroid(cluster, l, k);
    }
    free(cluster);
    free(new_centroids);
}