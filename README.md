# MINI_PROYECTO3

Santiago Torres Rojas - 2380301
Samuel Garcia Parra - 2459476
Juan Sebastian Navarrete Rada - 2459562
Juan David Correa Zapata - 2459431


Primeramente están los modelos, los cuales se limpiaron para que retornaran mas no imprimieran ni mostraran nada. Después se crearon las vistas, siendo VistaJuego la "general", en la cual se implementaron las funciones de las cuales la vista se iba a encargar de mostrar tanto para la vistaGUI como para la vistaTerminal. Después se creó la vistaTerminal, esta usaría las funciones de la VistaJuego e imprimiría mensajes que irán con el curso de la batalla. Después se hizo el controlador, el cual se implementó con la lógica que debía seguir la batalla, primeramente iniciándola y gestionando los turnos de héroes y enemigos. Por último, se creó la vistaGUI, en la cual se implementó una interfaz gráfica básica y con los métodos de VistaJuego. Si es muy perspicaz, se dará cuenta de que hay métodos vacíos ya sea en la vistaGUI o en la vistaTerminal; esto es porque hay métodos de VistaJuego que no se usan en una u otra, son únicos ya sea para la Terminal o la GUI. Y ya para finalizar, el app, el cual se encarga solo de crear los héroes y enemigos y preguntar al usuario qué interfaz desea ver, si la GUI o la Terminal, y según la selección, llamar el método iniciarBatalla desde Controlador.

