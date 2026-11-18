USE LIBRERIA;

-- 1. Insertar los 9 libros en la tabla 'libros'
INSERT INTO libros (titulo, autor, link_imagen, descripcion, disponible) VALUES
('La vegetariana', 'Han Kang', NULL, 'Yeonghye, una mujer corriente en Seúl, decide dejar de comer carne tras sufrir unas pesadillas perturbadoras, desencadenando una rebelión silenciosa que rompe con las convenciones familiares y sociales.', true),
('La ladrona de libros', 'Markus Zusak', NULL, 'Ambientada en la Alemania nazi y narrada por la propia Muerte, cuenta la historia de Liesel Meminger, una niña que encuentra consuelo y resistencia robando libros y compartiéndolos con los demás.', true),
('El perfume', 'Patrick Süskind', NULL, 'En la Francia del siglo XVIII, Jean-Baptiste Grenouille nace con un sentido del olfato prodigioso pero sin olor propio, lo que le lleva a una obsesión macabra por crear la fragancia perfecta.', true),
('El niño con el pijama de rayas', 'John Boyne', NULL, 'Bruno, el hijo de ocho años de un comandante nazi en Auschwitz, entabla una inocente pero trágica amistad a través de una alambrada con Shmuel, un niño judío prisionero en el campo.', true),
('L''amic retrobat', 'Fred Uhlman', NULL, 'En el Stuttgart de 1932, dos adolescentes de mundos opuestos —uno hijo de un médico judío y otro de una familia aristocrática alemana— forjan una amistad inquebrantable marcada por el ascenso del nazismo.', true),
('El lector', 'Bernhard Schlink', NULL, 'En la Alemania de posguerra, el joven Michael Berg mantiene una intensa relación con Hanna Schmitz, una mujer mucho mayor que guarda un oscuro secreto relacionado con el pasado nazi.', true),
('Los pilares de la Tierra', 'Ken Follett', NULL, 'Ambientada en la Inglaterra del siglo XII, narra la épica construcción de una catedral gótica en Kingsbridge entre intrigas políticas, guerras civiles y luchas de poder.', true),
('El código Da Vinci', 'Dan Brown', NULL, 'El simbologista Robert Langdon y la criptóloga Sophie Neveu investigan un asesinato en el Museo del Louvre que destapa una trama milenaria oculta en las obras de Leonardo da Vinci.', true),
('Aquitania', 'Eva García Sáenz de Urturi', NULL, 'Thriller histórico ambientado en la Europa medieval donde la joven duquesa Leonor de Aquitania trama su venganza tras el asesinato de su padre en Compostela.', true);

-- 2. Insertar 5 usuarios en la tabla 'usuarios' (1 ADMIN + 4 USER)
INSERT INTO usuarios (nombre, email, telefono, rol, password) VALUES
('Administrador', 'admin@libreria.com', '600111222', 'ADMIN', 'admin123'),
('Juan Perez', 'juan@libreria.com', '611222333', 'USER', '12345678a'),
('Laura Vidal', 'laura@libreria.com', '622333444', 'USER', '12345678a'),
('Jorge Garcia', 'jorge@libreria.com', '633444555', 'USER', '12345678a'),
('Clara Roca', 'clara@libreria.com', '644555666', 'USER', '12345678a');