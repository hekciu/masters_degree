import numpy as np
import matplotlib.pyplot as plt

alfa = (10 / 360) * 2 * np.pi
beta = (30 / 360) * 2 * np.pi
b = 0.73 * 0
g = 9.8
m = 0.5
R_engine = 5
u_max = 120
u_min = -120
dt = 0.001
V_desired = 25 # m/s


def u(e, e_sum):
    u_desired = 15 * e + 0.1 * e_sum
    if u_desired < u_min:
        return u_min
    if u_desired > u_max:
        return u_max

    return u_desired



def model(u_curr, v_prev, is_backwards = False): # returns v_curr
    # ma = Fc - Fz - Fop

    # Fc = (u[-1] * u[-1]) / (R * v[-1]) # handle v == 0
    # Fz = np.sin(alfa) * m * g
    # Fop = A * v[-1] * v[-1] # Fd = 0.5 ρ v^2 A Cd
    # a = (v[-1] - v[-2])/dt

    # m * (v[-1] - v[-2])/dt = (u[-1] * u[-1]) / (R * v[-1]) - np.sin(alfa) * m * g - A * v[-1] * v[-1] # * v[-1]
    # m * v[-1] * (v[-1] - v[-2])/dt + np.sin(alfa) * v[-1] * m * g + A * v[-1] * v[-1] * v[-1] = (u[-1] * u[-1]) / R
    
    # m * v_curr * (v_curr - v_prev) / dt + np.sin(alfa) * v_curr * m * g + A * v_curr * v_curr * v_curr = (u_curr * u_curr) / R
    # m * v_curr * (v_curr - v_prev) / dt + np.sin(alfa) * v_curr * m * g + A * v_curr * v_curr * v_curr - (u_curr * u_curr) / R = 0
    # m * v_curr * v_curr / dt - m * v_curr * v_prev / dt + np.sin(alfa) * v_curr * m * g + A * v_curr * v_curr * v_curr - (u_curr * u_curr) / R = 0

    # below add advanced formula for Fop
    # A * v_curr * v_curr * v_curr + m * v_curr * v_curr / dt + v_curr(-(m * v_prev) / dt + np.sin(alfa) * m * g) - (u_curr * u_curr) / R = 0
    
    # print([A, m/dt, -(m * v_prev)/ dt + np.sin(alfa) * m * g, - (u_curr * u_curr) / R_engine])
    output_roots = np.roots([b, m/dt, -(m * v_prev)/ dt + np.sin(alfa) * m * g, - (u_curr * u_curr) / R_engine])
    # print(u_curr)

    for root in output_roots:
        if root >= 0 and not is_backwards:
            return root
        if root <= 0 and is_backwards:
            return root


def calculate(u_arr, v_arr, e_sum):
    v_prev = v_arr[-1] 
    e = V_desired - v_prev 
    e_sum += e
    u_curr = u(e, e_sum) 
    v_curr = model(u_curr, v_prev)
    # print(e, u_curr, v_curr)
    v_arr = np.append(v_arr, v_curr)
    u_arr = np.append(u_arr, u_curr)
    return u_arr, v_arr, e_sum


def run():
    e_sum = 0
    u_arr = np.array([0]) # V
    v_arr = np.array([15]) # m/s
    t_arr = np.arange(0, 10, dt)
    for t in range(t_arr.shape[0] - 1):
        u_arr, v_arr, e_sum = calculate(u_arr, v_arr, e_sum)

    plt.plot(t_arr, v_arr)
    plt.xlabel('t[s]')  
    plt.ylabel('v[m/s]')  
        
    plt.title('')  
        
    plt.show()  

run()
