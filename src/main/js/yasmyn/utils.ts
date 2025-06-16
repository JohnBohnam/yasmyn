import AsyncStorage from "@react-native-async-storage/async-storage";
import { Alert, Platform } from "react-native";
import { User } from "./Model";
import { API_BASE_URL } from "./constants";


export const showAlert = (title: string, message: string, onConfirm?: () => void) => {
    if (Platform.OS === 'web') {
        window.alert(`${title}: ${message}`);
            if (onConfirm) onConfirm();
    } else {
        Alert.alert(title, message, [
            { text: 'OK', onPress: onConfirm || (() => {}) }
        ]);
    }
};

export const fetchMyInfo = async () => {
        const authToken = await AsyncStorage.getItem("authToken");
        if (!authToken) throw new Error("Authentication token is missing");

        try {
            const response = await fetch(`${API_BASE_URL}/me`, {
                method: "GET",
                headers: {Authorization: `Bearer ${authToken}`},
            });

            if (!response.ok) throw new Error("Failed to fetch user info");

            const userInfo: User = await response.json();
            return userInfo;
        } catch (error) {
            Alert.alert("Error", "Could not load user info");
        }
    };