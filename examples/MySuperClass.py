import math

class MySuperClass:
    numberOfInstances: int = 0
    
    def __init__(self):
        MySuperClass.__addNumberOfInstance()
        self.x: float = 0
        self.y: float = 0
        self.__name: str = "default_name"
        
    def __del__(self):
        MySuperClass.__subNumberOfInstance()
        
    def __addNumberOfInstance() -> None:
        MySuperClass.numberOfInstances = MySuperClass.numberOfInstances+1
        
    def __subNumberOfInstance() -> None:
        MySuperClass.numberOfInstances = MySuperClass.numberOfInstances-1
        
    def setName(self, name: str) -> None:
        if(name is str):
            self.__name = name
        
    def squared(self) -> None:
        self.x = self.x ^ 2
        self.y = self.y ^ 2
        
    def squareRoot(self) -> None:
        if self.x >= 0 and self.y >= 0:
            self.x = math.sqrt(self.x)
            self.y = math.sqrt(self.y)
        else:
            raise ValueError("x and y are negative")
        
    def __str__(self):
        return "MySuperClass[" \
            + "x:" + str(self.x) \
            + ",y:" + str(self.y) \
            + ",name:\"" + str(self.__name) + "\"]"

#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#
# Some functions out of the class #
#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#

def add(x: float, y: float) -> float:
    return x+y

def fibonacci(n: int) -> int:
    if(n <= 0): return 0
    if(n == 1): return 1
    return fibonacci(n-1) + fibonacci(n-2)

def pow(x: float, n: int) -> float:
    if(n == 0): return 1
    if(n > 0): return x * pow(x,n-1)
    return 1 / (pow(x,-n))