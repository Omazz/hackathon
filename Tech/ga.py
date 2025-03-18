import numpy as np
import pygad
import random

def generate_random_cargos(num_cargos, cargo_max_width, cargo_max_length, cargo_max_height, cargo_max_weight):
    cargos = []
    for _ in range(num_cargos):
        width = random.randint(1, cargo_max_width)
        length = random.randint(1, cargo_max_length)
        height = random.randint(1, cargo_max_height)
        weight = random.randint(1, cargo_max_weight)
        fragility = random.randint(0, 1)
        cargos.append((width, length, height, weight, fragility))
    return cargos


NUM_CARGOS = 1000       # Количество грузов
CARGO_MAX_WIDTH = 50    # Максимальная ширина груза
CARGO_MAX_LENGTH = 50   # Максимальная длина груза
CARGO_MAX_HEIGHT = 50   # Максимальная высота груза
CARGO_MAX_WEIGHT = 300  # Максимальный вес груза

cargos = generate_random_cargos(NUM_CARGOS, CARGO_MAX_WIDTH, CARGO_MAX_LENGTH, CARGO_MAX_HEIGHT, CARGO_MAX_WEIGHT)

# Грузовой отсек
CARGO_HOLD_WIDTH = 10
CARGO_HOLD_LENGTH = 10
CARGO_HOLD_HEIGHT = 10
CARGO_HOLD_CAPACITY = NUM_CARGOS * CARGO_MAX_WEIGHT

def fitness_func(ga_instance, solution, solution_idx):
    total_weight = 0
    total_volume = 0
    total_cargos = 0
    coef_total_weight = 1.
    coef_total_cargos = 1000.
    
    for i in range(NUM_CARGOS):
        if solution[i] == 1:
            width, length, height, weight, fragility = cargos[i]
            total_weight += weight
            if total_weight > CARGO_HOLD_CAPACITY:
                return 1
            
            total_volume += width * length * height
            if total_volume > CARGO_HOLD_WIDTH * CARGO_HOLD_LENGTH * CARGO_HOLD_HEIGHT:
                return 1
            
            total_cargos += 1
            
    fitness = (total_weight * coef_total_weight) + (total_cargos * coef_total_cargos)
    return max(1, 1+fitness)


num_generations = 1000
num_parents_mating = 5
sol_per_pop = 1000
num_genes = NUM_CARGOS
init_range_low = 0
init_range_high = 2
parent_selection_type = "rws"
keep_parents = 2
crossover_type = "uniform" # two_points не подходит, не хватает случайности
mutation_type = "random"
mutation_percent_genes = 10

ga_instance = pygad.GA(
    num_generations=num_generations,
    num_parents_mating=num_parents_mating,
    fitness_func=fitness_func,
    sol_per_pop=sol_per_pop,
    num_genes=num_genes,
    init_range_low=init_range_low,
    init_range_high=init_range_high,
    parent_selection_type=parent_selection_type,
    keep_parents=keep_parents,
    crossover_type=crossover_type,
    mutation_type=mutation_type,
    mutation_percent_genes=mutation_percent_genes,
    gene_type=int
)

ga_instance.run()

solution, solution_fitness, solution_idx = ga_instance.best_solution()
print(f"Лучшее решение: {solution}")
print(f"Фитнес-функция лучшего решения: {solution_fitness}")

ga_instance.plot_fitness()