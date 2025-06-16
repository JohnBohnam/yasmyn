import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, Platform } from 'react-native';
import {API_BASE_URL} from "../constants";
import { showAlert } from '../utils';

// @ts-ignore
export default function RegisterScreen({ navigation }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [username, setUsername] = useState('');


    const handleRegister = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password,
                }),
            });

            if (response.ok) {
                showAlert('Success', 'Registered successfully!', () => {
                    navigation.navigate('Login');
                });
                console.log('Registration successful');
            } else {
                console.log('womp.1');
                const error = await response.json();
                showAlert('Error', error.message || 'Registration failed');
                console.log('Registration failed', error);
            }
        } catch (err: any) {
            console.log('womp.2');
            showAlert('Error', err.message);
        }
    };


    return (
        <View style={styles.container}>
            <Text style={styles.title}>Register</Text>
            <TextInput
                style={styles.input}
                placeholder="Email"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
            />
            <TextInput
                style={styles.input}
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
            />
            <TextInput
                style={styles.input}
                placeholder="Password"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
            />
            <Button title="Register" onPress={handleRegister} />
            <Text style={styles.link} onPress={() => navigation.navigate('Login')}>
                Already have an account? Login
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, justifyContent: 'center', padding: 20 },
    title: { fontSize: 24, marginBottom: 20, textAlign: 'center' },
    input: { borderWidth: 1, borderColor: '#ccc', padding: 10, marginBottom: 15, borderRadius: 5 },
    link: { marginTop: 15, color: 'blue', textAlign: 'center' },
});