# testquicksas
Esta aplicación se desarrollo como requisito del proceso de selección para desarrollador androi de la empresa Quick

La aplicación implementa las siguientes funcionalidades:

- Desarrollada en Kotlin
- Arquitectura MVVM
- Implementa Firebase para autenticación y registro de usuarios
- Funcionalidad para un chat de grupo de todos los usuarios registrados
- Funcionalidad de chat individual con cualquier usuario de la app
- Se pueden enviar textos e imágenes
- Almacenamiento en Cloud Firestore
- El chat existe en tiempo real
- La app realiza la persistencia de datos de usuarios, sesión y chats con el fin de visualizarlos sin conexión
- Se trabajo con componentes de Android Material
- Se implemento la nueva forma de navegación que ofrece Android con el BottomNavigationView de Android Jetpack

ARQUITECTURA

- Se trabajo la arquitectura MVVM la cual es una de las mas usadas en el momento, y que también Android Jetpack la implementa, las ventajas de esta arquitectura es que nos permite evitar problemas de ciclos de vida de actividades o fragmentos. También nos brinda una moularización optima, entendible y adecuada a nuestro proyecto, adicional a esto, con esta arquitectura no recargamos de muchas funcionalidades nuestra vista sino simplemente la de mostrar la información, cumpliendo así con el primer principio de SOLID que es el de "responsabilidad única"

FUNCIONAMIENTO

- En cuanto a funcionamiento, la aplicación corre perfectamente y cumple con los objetivos de la prueba. Se probó cuidadosamente cada funcionalidad y se valido que no existieran crash. Adicional a esto, en ciertas funcionalidades se implemento un manejo de excepciones esto con el fin de evitar que la app se detenga.

VISTAS

Cuenta con:

- Vista de login
- Vista de registro
- Vista Principal que implementa la navegación
- Un Fragment para el chat general o grupal, en esta vista esta la opción de buscar un usuario registrado para entablar una conversación, solo debes ingresar el nombre y presionar sobre el item de la lista que prefieras.
- Un Fragment que lista los chats que cada persona ha tenido con otros usuarios, al dar click en un item de la lista se lleva el usuario a otra vista que es el chat privado con el usuario seleccionado.
- Un Fragment donde permite actualizar la información del perfil y la opción de cerrar sesión

LINK DESCARGA APK

- https://drive.google.com/file/d/1uFdsFc2rWEDJBjFbMpA1fQQHaVfGiopR/view?usp=sharing
