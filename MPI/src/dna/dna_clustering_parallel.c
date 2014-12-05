#include "mpi.h"
#include "kmeans.h"

int main(int argc, char **argv) {
	int num_tasks, rank, len, rc;
	MPI_Status Stat;
	char hostname[MPI_MAX_PROCESSOR_NAME];

	rc = MPI_Init(&argc, &argv);
	if (rc != MPI_SUCCESS) {
		printf ("Error starting MPI program. Terminating.\n");
		MPI_Abort(MPI_COMM_WORLD, rc);
	}

	MPI_Comm_size(MPI_COMM_WORLD, &num_tasks);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Get_processor_name(hostname, &len);
	printf ("Number of tasks= %d My rank= %d Running on %s\n", num_tasks, rank, hostname);

	dna_t *dataset;
	dna_t *centroids;
	int k, num_of_points, l;
	int startflag = 1;

	config_t *config = read_config();
    k = config->k;
    num_of_points = config->count;
    l = config->l;

	if (rank == 0) {
		clock_t start, end;
     	double cpu_time_used;
     	start = clock();

		char base[4] = {'A', 'C', 'G', 'T'};
    	dataset = init_dataset(config);
    	centroids = init_centroid(dataset, l, k, num_of_points);
    	cluster_sum_t *cluster_sum = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));
    	int points_per_machine = num_of_points / (num_tasks - 1);
    	int remaining_points = num_of_points % (num_tasks - 1);
    	int points_sent = 0;
    	// send dataset to slaves
    	for (int i = 1; i < num_tasks; i++) {
    		int send_count = points_per_machine;
    		if (i == num_tasks - 1) {
    			send_count += remaining_points;
    		}
    		// send out the count of dataset
    		rc = MPI_Send(&send_count, 1, MPI_INT, i, 1, MPI_COMM_WORLD);
    		for (int j = 0; j < send_count; j++) {
    			rc = MPI_Send(dataset[points_sent].acid, l, MPI_CHAR, i, 2, MPI_COMM_WORLD);
    			points_sent++;
    		}
    	}
    	dna_t *new_centroids = (dna_t *)malloc(k * sizeof(dna_t));
    	do {
    		for (int i = 1; i < num_tasks; i++) {
    			rc = MPI_Send(&startflag, 1, MPI_INT, i, 3, MPI_COMM_WORLD);
    			for (int j = 0; j < k; j++) {
    				rc = MPI_Send(centroids[j].acid, l, MPI_CHAR, i, 4, MPI_COMM_WORLD);
    			}
    		}
    		for (int i = 0; i < k; i++) {
    			cluster_sum[i].count = (int **)malloc(l * sizeof(int *));
        		for (int j = 0; j < l; j++) {
            		cluster_sum[i].count[j] = (int *)malloc(4 * sizeof(int));
            		for (int a = 0; a < 4; a++) {
                		cluster_sum[i].count[j][a] = 0;
            		}
       			}
     		}
    		for (int i = 1; i < num_tasks; i++) {
    			for (int j = 0; j < k; j++) {
    				for (int a = 0; a < l; a++) {
    					for (int b = 0; b < 4; b++) {
    						rc = MPI_Recv(&cluster_sum[j].count[a][b], 1, MPI_INT, i, 5, MPI_COMM_WORLD, &Stat);
    					}
    				}
    			}
    		}
    		for (int i = 0; i < k; i++) {
        		cluster_sum_t ccluster = cluster_sum[i];
        		dna_t centroid;
		        char *content = (char *)malloc(l * sizeof(char));
		        for (int j = 0; j < l; j++) {
		            int count = ccluster.count[j][0];
		            int index = 0;
		            for (int a = 1; a < 4; a++) {
		                int temp = ccluster.count[j][a];
		                if (count < temp) {
		                    count = temp;
		                    index = a;
		                }
		            }
		            content[j] = base[index];
		        }
		        centroid.acid = content;
		        new_centroids[i] = centroid;
    		}
    	} while (!compare_replace(centroids, new_centroids, l, k));
    	startflag = 0;
    	for (int i = 1; i < num_tasks; i++) {
    		rc = MPI_Send(&startflag, 1, MPI_INT, i, 3, MPI_COMM_WORLD);
    	}
    	free(new_centroids);
    	free(cluster_sum);
    	printf("Final Result:\n");
    	print_result(centroids, l, k);
    	write_result(centroids, l, k, config);
    	end = clock();
     	cpu_time_used = ((double) (end - start)) / CLOCKS_PER_SEC;
     	printf("Total time used: %lf\n", cpu_time_used);
	}
	else {
		// receive number of points of dataset
		rc = MPI_Recv(&num_of_points, 1, MPI_INT, 0, 1, MPI_COMM_WORLD, &Stat);
		printf("Slave %d: Received %d data points\n", rank, num_of_points);
		dataset = (dna_t *)malloc(num_of_points * sizeof(dna_t));
		for (int i = 0; i < num_of_points; i++) {
			dataset[i].acid = (char *)malloc(l * sizeof(char));
			// receive each dataset content
			rc = MPI_Recv(dataset[i].acid, l, MPI_CHAR, 0, 2, MPI_COMM_WORLD, &Stat);
		}
		rc = MPI_Recv(&startflag, 1, MPI_INT, 0, 3, MPI_COMM_WORLD, &Stat);
		centroids = (dna_t *)malloc(k * sizeof(dna_t));
		while (startflag) {
			for (int i = 0; i < k; i++) {
				centroids[i].acid = (char *)malloc(l * sizeof(char));
				rc = MPI_Recv(centroids[i].acid, l, MPI_CHAR, 0, 4, MPI_COMM_WORLD, &Stat);
			}
			node_t **cluster = compute_cluster(dataset, centroids, l, k, num_of_points);
			cluster_sum_t *cluster_sum = compute_cluster_sum(cluster, l, k);
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < l; j++) {
					for (int b = 0; b < 4; b++) {
						rc = MPI_Send(&cluster_sum[i].count[j][b], 1, MPI_INT, 0, 5, MPI_COMM_WORLD);
					}
				}
			}
			free(cluster_sum);
			free(cluster);
			rc = MPI_Recv(&startflag, 1, MPI_INT, 0, 3, MPI_COMM_WORLD, &Stat);
		}
		free(centroids);
		free(dataset);
	}
	MPI_Finalize();
	return 0;
}